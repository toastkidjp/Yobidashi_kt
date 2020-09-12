package jp.toastkid.yobidashi.browser.bookmark

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.permission.RuntimePermissions
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RecyclerViewScroller
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.databinding.FragmentBookmarkBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Okio

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

    private var contentViewModel: ContentViewModel? = null

    private val disposables: Job by lazy { Job() }

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
                { history -> contentViewModel?.snackShort(history.title) },
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
                        val fromPos = viewHolder.absoluteAdapterPosition
                        val toPos = target.absoluteAdapterPosition
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
                        adapter.removeAt(viewHolder.absoluteAdapterPosition)
                    }
                }).attachToRecyclerView(binding.historiesView)

        adapter.showRoot()

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            contentViewModel = ViewModelProvider(it).get(ContentViewModel::class.java)
        }
    }

    /**
     * Finish this activity with result.
     *
     * @param uri
     */
    private fun finishWithResult(uri: Uri) {
        popBackStack()
        ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java).open(uri)
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.bookmark, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragmentManager = fragmentManager ?: return true
        return when (item.itemId) {
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
                CoroutineScope(Dispatchers.Main).launch(disposables) {
                    RuntimePermissions(requireActivity())
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            ?.receiveAsFlow()
                            ?.collect {
                                permissionResult ->
                                if (!permissionResult.granted) {
                                    contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                                    return@collect
                                }

                                startActivityForResult(
                                        IntentFactory.makeGetContent("text/html"),
                                        REQUEST_CODE_IMPORT_BOOKMARK
                                )
                            }
                }
                true
            }
            R.id.export_bookmark -> {
                CoroutineScope(Dispatchers.Main).launch(disposables) {
                    RuntimePermissions(requireActivity())
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            ?.receiveAsFlow()
                            ?.collect { permissionResult ->
                                if (!permissionResult.granted) {
                                    contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                                    return@collect
                                }

                                startActivityForResult(
                                        IntentFactory.makeDocumentOnStorage(
                                                "text/html", "bookmark.html"),
                                        REQUEST_CODE_EXPORT_BOOKMARK
                                )
                            }
                }
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
        adapter.clearAll{ contentViewModel?.snackShort(R.string.done_clear) }
    }

    override fun onClickAddDefaultBookmark() {
        BookmarkInitializer()(binding.root.context) { adapter.showRoot() }
        contentViewModel?.snackShort(R.string.done_addition)
    }

    override fun onClickAddFolder(title: String?) {
        if (TextUtils.isEmpty(title)) {
            return
        }

        CoroutineScope(Dispatchers.Main).launch(disposables) {
            BookmarkInsertion(
                    binding.root.context,
                    title ?: "", // This value is always non-null, because it has checked at above statement.
                    parent = adapter.currentFolderName(),
                    folder = true
            ).insert()

            adapter.reload()
        }
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

        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) {
                ExportedFileParser()(inputStream).forEach { bookmarkRepository.add(it) }

                adapter.showRoot()
                contentViewModel?.snackShort(R.string.done_addition)
            }
        }
    }

    /**
     * Export bookmark.
     *
     * @param uri
     */
    private fun exportBookmark(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            val items = withContext(Dispatchers.IO) { bookmarkRepository.all() }
            val outputStream = context?.contentResolver?.openOutputStream(uri) ?: return@launch
            Okio.buffer(Okio.sink(outputStream)).use {
                it.writeUtf8(Exporter(items).invoke())
            }
        }
    }

    override fun onDetach() {
        adapter.dispose()
        disposables.cancel()
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