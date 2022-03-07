package jp.toastkid.yobidashi.browser.bookmark

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.annotation.WorkerThread
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.intent.CreateDocumentIntentFactory
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RecyclerViewScroller
import jp.toastkid.lib.view.swipe.SwipeActionAttachment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.FaviconFolderProviderService
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.icon.WebClipIconLoader
import jp.toastkid.yobidashi.databinding.FragmentBookmarkBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink

/**
 * Bookmark list activity.
 *
 * @author toastkidjp
 */
class BookmarkFragment: Fragment(),
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

    private val getContentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.data == null || it.resultCode != Activity.RESULT_OK) {
            return@registerForActivityResult
        }

        val uri = it.data?.data ?: return@registerForActivityResult
        importBookmark(uri)
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.data == null || it.resultCode != Activity.RESULT_OK) {
            return@registerForActivityResult
        }

        val uri = it.data?.data ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch(disposables) {
            exportBookmark(uri)
        }
    }

    private val importRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                return@registerForActivityResult
            }

            getContentLauncher.launch(GetContentIntentFactory()("text/html"))
        }

    private val exportRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                return@registerForActivityResult
            }

            exportLauncher.launch(
                CreateDocumentIntentFactory()("text/html", "bookmark.html")
            )
        }

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
        SwipeActionAttachment().invoke(binding.historiesView)

        adapter.showRoot()

        setHasOptionsMenu(true)

        parentFragmentManager.setFragmentResultListener(
            "clear_bookmark",
            viewLifecycleOwner,
            { key, results ->
                if (results[key] != true) {
                    return@setFragmentResultListener
                }
                adapter.clearAll{ contentViewModel?.snackShort(R.string.done_clear) }
            }
        )
        parentFragmentManager.setFragmentResultListener(
            "import_default",
            viewLifecycleOwner,
            { _, _ ->
                val currentContext = binding.root.context
                BookmarkInitializer(
                    FaviconFolderProviderService().invoke(currentContext),
                    WebClipIconLoader.from(context)
                )(currentContext) { adapter.showRoot() }
                contentViewModel?.snackShort(R.string.done_addition)
            }
        )

        parentFragmentManager.setFragmentResultListener(
            "adding_folder",
            viewLifecycleOwner,
            { key, results ->
                val title = results.getString(key)
                if (title.isNullOrEmpty()) {
                    return@setFragmentResultListener
                }

                CoroutineScope(Dispatchers.Main).launch(disposables) {
                    BookmarkInsertion(
                        binding.root.context,
                        title,
                        parent = adapter.currentFolderName(),
                        folder = true
                    ).insert()

                    adapter.reload()
                }
            }
        )

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
        activity?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java).open(uri)
        }
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.bookmark, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragmentManager = parentFragmentManager
        return when (item.itemId) {
            R.id.clear -> {
                ConfirmDialogFragment.show(
                    fragmentManager,
                    getString(R.string.title_clear_bookmark),
                    Html.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    ),
                    "clear_bookmark"
                )
                true
            }
            R.id.add_default -> {
                ConfirmDialogFragment.show(
                    fragmentManager,
                    getString(R.string.title_add_default_bookmark),
                    getString(R.string.message_add_default_bookmark),
                    "import_default"
                )
                true
            }
            R.id.add_folder -> {
                AddingFolderDialogFragment().also {
                    it.show(
                            fragmentManager,
                            AddingFolderDialogFragment::class.java.simpleName
                    )
                }
                true
            }
            R.id.import_bookmark -> {
                importRequestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                true
            }
            R.id.export_bookmark -> {
                exportRequestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
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
            val faviconApplier = FaviconApplier(context)
            withContext(Dispatchers.IO) {
                ExportedFileParser()(inputStream)
                    .map {
                        it.favicon = faviconApplier.makePath(it.url)
                        it
                    }
                    .forEach { bookmarkRepository.add(it) }

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
    @WorkerThread
    private fun exportBookmark(uri: Uri) {
        val items = bookmarkRepository.all()
        val outputStream = context?.contentResolver?.openOutputStream(uri) ?: return
        outputStream.sink().use { sink ->
            sink.buffer().use {
                it.writeUtf8(Exporter(items).invoke())
            }
        }
    }

    override fun onDetach() {
        adapter.dispose()
        disposables.cancel()
        getContentLauncher.unregister()
        exportLauncher.unregister()
        importRequestPermissionLauncher.unregister()
        exportRequestPermissionLauncher.unregister()

        parentFragmentManager.clearFragmentResultListener("clear_bookmark")
        parentFragmentManager.clearFragmentResultListener("import_default")
        parentFragmentManager.clearFragmentResultListener("adding_folder")

        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_bookmark

    }
}