package jp.toastkid.yobidashi.browser.bookmark

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.MenuItem
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBookmarkBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import okio.Okio
import timber.log.Timber

/**
 * Bookmark list activity.
 *
 * @author toastkidjp
 */
class BookmarkActivity: BaseActivity(),
        BookmarkClearDialogFragment.OnClickBookmarkClearCallback,
        DefaultBookmarkDialogFragment.OnClickDefaultBookmarkCallback,
        AddingFolderDialogFragment.OnClickAddingFolder
{

    /**
     * Data binding object.
     */
    private lateinit var binding: ActivityBookmarkBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: ActivityAdapter

    /**
     * Composite of disposables.
     */
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        val relation = DbInitter.init(this).relationOfBookmark()

        binding.historiesView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> Toaster.snackShort(binding.root, history.title, colorPair()) },
                binding.historiesView::scheduleLayoutAnimation
        )
        binding.historiesView.adapter = adapter
        binding.historiesView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int) = false
        }
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        val fromPos = viewHolder.adapterPosition
                        val toPos = target.adapterPosition
                        adapter.notifyItemMoved(fromPos, toPos)
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        if (direction != ItemTouchHelper.RIGHT) {
                            return
                        }
                        adapter.removeAt(viewHolder.adapterPosition)
                    }
                }).attachToRecyclerView(binding.historiesView)

        binding.toolbar.let {
            initToolbar(it)
            it.setNavigationOnClickListener { onBackPressed() }
            it.inflateMenu(R.menu.bookmark)
        }

        adapter.showRoot()
    }

    /**
     * Finish this activity with result.
     *
     * @param uri
     */
    private fun finishWithResult(uri: Uri) {
        val intent = Intent()
        intent.setData(uri)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()

        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                BookmarkClearDialogFragment()
                        .show(
                                supportFragmentManager,
                                BookmarkClearDialogFragment::class.java.simpleName
                        )
                return true
            }
            R.id.add_default -> {
                DefaultBookmarkDialogFragment()
                        .show(
                                supportFragmentManager,
                                DefaultBookmarkDialogFragment::class.java.simpleName
                        )
                return true
            }
            R.id.add_folder -> {
                AddingFolderDialogFragment().show(
                        supportFragmentManager,
                        AddingFolderDialogFragment::class.java.simpleName
                )
            }
            R.id.import_bookmark -> {
                RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(
                                { granted ->
                                    if (!granted) {
                                        Toaster.snackShort(
                                                binding.root,
                                                R.string.message_requires_permission_storage,
                                                colorPair()
                                        )
                                        return@subscribe
                                    }
                                    startActivityForResult(
                                            IntentFactory.makeGetContent("text/html"),
                                            REQUEST_CODE_IMPORT_BOOKMARK
                                    )
                                },
                                { Timber.e(it) }
                        )
                        .addTo(disposables)
                return true
            }
            R.id.export_bookmark -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    Toaster.snackShort(binding.root, R.string.message_disusable_menu, colorPair())
                    return true
                }
                RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(
                                { granted ->
                                    if (!granted) {
                                        Toaster.snackShort(
                                                binding.root,
                                                R.string.message_requires_permission_storage,
                                                colorPair()
                                        )
                                        return@subscribe
                                    }
                                    startActivityForResult(
                                        IntentFactory.makeDocumentOnStorage(
                                                "text/html", "bookmark.html"),
                                        REQUEST_CODE_EXPORT_BOOKMARK
                                    )
                                },
                                { Timber.e(it) }
                        )
                        .addTo(disposables)
                return true
            }
            R.id.to_top -> {
                RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
                return true
            }
            R.id.to_bottom -> {
                RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
                return true
            }
        }
        return super.clickMenu(item)
    }

    override fun onBackPressed() {
        if (adapter.back()) {
            return
        }
        super.onBackPressed()
    }

    override fun onClickBookmarkClear() {
        adapter.clearAll{ Toaster.snackShort(binding.root, R.string.done_clear, colorPair())}
    }

    override fun onClickAddDefaultBookmark() {
        BookmarkInitializer(this)
        adapter.showRoot()
        Toaster.snackShort(binding.root, R.string.done_addition, colorPair())
    }

    override fun onClickAddFolder(title: String?) {
        if (TextUtils.isEmpty(title)) {
            return
        }
        Completable.fromAction {
            BookmarkInsertion(
                    this,
                    title as String, // This value is always non-null, because it has checked at above statement.
                    parent = adapter.currentFolderName(),
                    folder = true
            ).insert() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { adapter.reload() }
                .addTo(disposables)
    }

    override fun titleId(): Int = R.string.title_bookmark

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (intent == null || resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            REQUEST_CODE_IMPORT_BOOKMARK -> importBookmark(intent.data)
            REQUEST_CODE_EXPORT_BOOKMARK -> exportBookmark(intent.data)
        }
    }

    /**
     * Import bookmark from selected HTML file.
     *
     * @param uri Bookmark exported html's Uri.
     */
    private fun importBookmark(uri: Uri) {
        Completable.fromAction {
            ExportedFileParser(contentResolver.openInputStream(uri)).forEach {
                BookmarkInsertion(
                        this,
                        title  = it.title,
                        url    = it.url,
                        folder = it.folder,
                        parent = it.parent
                ).insert()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            adapter.reload()
                            Toaster.snackShort(binding.root, R.string.done_addition, colorPair())
                        },
                        { Timber.e(it) }
                ).addTo(disposables)
    }

    /**
     * Export bookmark.
     *
     * @param uri
     */
    private fun exportBookmark(uri: Uri) {
        val bookmarks = DbInitter.init(this).relationOfBookmark().selector().toList()
        Okio.buffer(Okio.sink(contentResolver.openOutputStream(uri))).run {
            writeUtf8(Exporter(bookmarks).invoke())
            flush()
            close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
        disposables.clear()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_bookmark

        /**
         * Request code.
         */
        const val REQUEST_CODE: Int = 202

        /**
         * Request code of importing bookmarks.
         */
        private const val REQUEST_CODE_IMPORT_BOOKMARK = 12211

        /**
         * Request code of exporting bookmarks.
         */
        private const val REQUEST_CODE_EXPORT_BOOKMARK = 12212

        /**
         * Make launching intent.
         *
         * @param context [Context]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BookmarkActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}