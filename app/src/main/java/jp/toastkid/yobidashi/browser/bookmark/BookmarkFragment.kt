package jp.toastkid.yobidashi.browser.bookmark

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.databinding.FragmentBookmarkBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.ContentScrollable
import okio.Okio
import timber.log.Timber

/**
 * Bookmark list activity.
 *
 * @author toastkidjp
 */
class BookmarkFragment: Fragment(),
        BookmarkClearDialogFragment.OnClickBookmarkClearCallback,
        DefaultBookmarkDialogFragment.OnClickDefaultBookmarkCallback,
        AddingFolderDialogFragment.OnClickAddingFolder,
        CommonFragmentAction,
        ContentScrollable
{

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentBookmarkBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var bookmarkRepository: BookmarkRepository

    /**
     * Composite of disposables.
     */
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        bookmarkRepository = DatabaseFinder().invoke(context).bookmarkRepository()

        binding.historiesView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = ActivityAdapter(
                context,
                bookmarkRepository,
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> Toaster.snackShort(binding.root, history.title, preferenceApplier.colorPair()) },
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

        adapter.showRoot()

        setHasOptionsMenu(true)

        return binding.root
    }

    /**
     * Finish this activity with result.
     *
     * @param uri
     */
    private fun finishWithResult(uri: Uri) {
        popBackStack()
        ViewModelProviders.of(requireActivity()).get(BrowserViewModel::class.java).open(uri)
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.bookmark, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.clear -> {
                BookmarkClearDialogFragment()
                        .also {
                            it.setTargetFragment(this, 1)
                            it.show(
                                    fragmentManager,
                                    BookmarkClearDialogFragment::class.java.simpleName
                            )
                        }
                true
            }
            R.id.add_default -> {
                DefaultBookmarkDialogFragment()
                        .also {
                            it.setTargetFragment(this, 2)
                            it.show(
                                    fragmentManager,
                                    DefaultBookmarkDialogFragment::class.java.simpleName
                            )
                        }
                true
            }
            R.id.add_folder -> {
                AddingFolderDialogFragment().also {
                    it.setTargetFragment(this, 3)
                    it.show(
                            fragmentManager,
                            AddingFolderDialogFragment::class.java.simpleName
                    )
                }
                true
            }
            R.id.import_bookmark -> {
                RxPermissions(requireActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(
                                { granted ->
                                    if (!granted) {
                                        /* TODO
                                        Toaster.snackShort(
                                                binding.root,
                                                R.string.message_requires_permission_storage,
                                                preferenceApplier.colorPair()
                                        )*/
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
                true
            }
            R.id.export_bookmark -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    // TODO Toaster.snackShort(binding.root, R.string.message_disusable_menu, preferenceApplier.colorPair())
                    return true
                }
                RxPermissions(requireActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(
                                { granted ->
                                    if (!granted) {
                                        /* TODO
                                        Toaster.snackShort(
                                                binding.root,
                                                R.string.message_requires_permission_storage,
                                                preferenceApplier.colorPair()
                                        )*/
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
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun pressBack(): Boolean {
        if (adapter.back()) {
            return true
        }
        return super.pressBack()
    }

    override fun onClickBookmarkClear() {
        adapter.clearAll{ Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair())}
    }

    override fun onClickAddDefaultBookmark() {
        BookmarkInitializer()(binding.root.context) { adapter.showRoot() }
                .addTo(disposables)

        Toaster.snackShort(binding.root, R.string.done_addition, preferenceApplier.colorPair())
    }

    override fun onClickAddFolder(title: String?) {
        if (TextUtils.isEmpty(title)) {
            return
        }
        Completable.fromAction {
            BookmarkInsertion(
                    binding.root.context,
                    title ?: "", // This value is always non-null, because it has checked at above statement.
                    parent = adapter.currentFolderName(),
                    folder = true
            ).insert() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { adapter.reload() }
                .addTo(disposables)
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent == null || resultCode != Activity.RESULT_OK) {
            return
        }

        val data = intent.data ?: return

        when (requestCode) {
            REQUEST_CODE_IMPORT_BOOKMARK -> importBookmark(data)
            REQUEST_CODE_EXPORT_BOOKMARK -> exportBookmark(data)
        }
    }

    /**
     * Import bookmark from selected HTML file.
     *
     * @param uri Bookmark exported html's Uri.
     */
    private fun importBookmark(uri: Uri) {
        val context = context ?: return
        val inputStream = context.contentResolver?.openInputStream(uri) ?: return

        Completable.using(
                { inputStream },
                {
                    Completable.fromAction {
                        ExportedFileParser()(it).forEach { bookmarkRepository.add(it) }
                    }
                },
                { it.close() }
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            adapter.showRoot()
                            /*TODO use common parent
                               Toaster.snackShort(
                                    binding.root,
                                    R.string.done_addition,
                                    preferenceApplier.colorPair()
                            )*/
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
        Maybe.fromCallable { bookmarkRepository.all() }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { bookmarks ->
                            val outputStream = context?.contentResolver?.openOutputStream(uri) ?: return@subscribe
                            Okio.buffer(Okio.sink(outputStream)).use {
                                it.writeUtf8(Exporter(bookmarks).invoke())
                            }
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    override fun onDetach() {
        adapter.dispose()
        disposables.clear()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_bookmark

        /**
         * Request code of importing bookmarks.
         */
        private const val REQUEST_CODE_IMPORT_BOOKMARK = 12211

        /**
         * Request code of exporting bookmarks.
         */
        private const val REQUEST_CODE_EXPORT_BOOKMARK = 12212

    }
}