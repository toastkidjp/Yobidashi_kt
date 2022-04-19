package jp.toastkid.yobidashi.browser.bookmark

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.intent.CreateDocumentIntentFactory
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.ui.list.SwipeToDismissItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.IOException
import java.util.Stack

/**
 * Bookmark list activity.
 *
 * @author toastkidjp
 */
class BookmarkFragment: Fragment(),
        CommonFragmentAction,
        ContentScrollable
{

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

    /**
     * Folder moving history.
     */
    private val folderHistory: Stack<String> = Stack()

    private val currentBookmarks = mutableStateListOf<Bookmark>()

    private var scrollState: LazyListState? = null

    @OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val activityContext = activity
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        preferenceApplier = PreferenceApplier(activityContext)

        bookmarkRepository = DatabaseFinder().invoke(activityContext).bookmarkRepository()

        showRoot()

        setHasOptionsMenu(true)

        activity?.let {
            contentViewModel = ViewModelProvider(it).get(ContentViewModel::class.java)
        }

        parentFragmentManager.setFragmentResultListener(
            "clear_bookmark",
            viewLifecycleOwner,
            { key, results ->
                if (results[key] != true) {
                    return@setFragmentResultListener
                }
                clearAll{ contentViewModel?.snackShort(R.string.done_clear) }
            }
        )
        parentFragmentManager.setFragmentResultListener(
            "import_default",
            viewLifecycleOwner,
            { _, _ ->
                BookmarkInitializer.from(activityContext)() { showRoot() }
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
                        activityContext,
                        title,
                        parent = currentFolderName(),
                        folder = true
                    ).insert()

                    reload()
                }
            }
        )

        return ComposeViewFactory().invoke(activityContext) {
            val bookmarks = remember { currentBookmarks }
            val listState = rememberLazyListState()
            this.scrollState = listState

            MaterialTheme() {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 4.dp),
                    modifier = Modifier
                        .nestedScroll(rememberViewInteropNestedScrollConnection())
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    items(bookmarks) { bookmark ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToStart) {
                                    try {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            bookmarkRepository.delete(bookmark)
                                        }
                                    } catch (e: IOException) {
                                        Timber.e(e)
                                    }
                                }
                                true
                            }
                        )
                        SwipeToDismissItem(
                            dismissState,
                            dismissContent = {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .combinedClickable(
                                            true,
                                            onClick = {
                                                if (bookmark.folder) {
                                                    folderHistory.push(bookmark.parent)
                                                    findByFolderName(bookmark.title)
                                                } else {
                                                    finishWithResult(Uri.parse(bookmark.url))
                                                }
                                            },
                                            onLongClick = {
                                                ViewModelProvider(activityContext)
                                                    .get(BrowserViewModel::class.java)
                                                    .openBackground(
                                                        bookmark.title,
                                                        Uri.parse(bookmark.url)
                                                    )
                                            }
                                        )
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                ) {
                                    AsyncImage(
                                        bookmark.favicon,
                                        bookmark.title,
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.Center,
                                        placeholder = painterResource(id = if (bookmark.folder) R.drawable.ic_folder_black else R.drawable.ic_bookmark),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Column() {
                                        Text(
                                            text = bookmark.title,
                                            fontSize = 18.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (bookmark.url.isNotBlank()) {
                                            Text(
                                                text = bookmark.url,
                                                color = colorResource(id = R.color.link_blue),
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (bookmark.lastViewed != 0L) {
                                            Text(
                                                text = DateFormat.format(
                                                    stringResource(R.string.date_format),
                                                    bookmark.lastViewed
                                                ).toString(),
                                                color = colorResource(id = R.color.gray_500_dd),
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }

    private fun findByFolderName(title: String) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            val items = withContext(Dispatchers.IO) {
                bookmarkRepository.findByParent(title)
            }
            submitList(items)
        }
    }

    /**
     * Reload.
     */
    private fun reload() {
        findByFolderName(currentFolderName())
    }

    /**
     * Remove item.
     *
     * @param item [Bookmark]
     */
    private fun remove(item: Bookmark) {
        val copy = ArrayList<Bookmark>(currentBookmarks)
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) { bookmarkRepository.delete(item) }

            copy.remove(item)
            submitList(copy)
        }
    }

    /**
     * Back to previous folder.
     */
    private fun back(): Boolean {
        if (folderHistory.isEmpty()) {
            return false
        }
        findByFolderName(folderHistory.pop())
        return true
    }

    /**
     * Clear all items.
     *
     * @param onComplete callback
     */
    private fun clearAll(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) { bookmarkRepository.clear() }

            onComplete()
            submitList(emptyList())
        }
    }

    private fun submitList(items: List<Bookmark>) {
        currentBookmarks.clear()
        currentBookmarks.addAll(items)
    }

    /**
     * Return current folder name.
     */
    private fun currentFolderName(): String =
        if (currentBookmarks.isEmpty() && folderHistory.isNotEmpty()) folderHistory.peek()
        else if (currentBookmarks.isEmpty()) Bookmark.getRootFolderName()
        else currentBookmarks[0].parent

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
        if (back()) {
            return true
        }
        return super.pressBack()
    }

    override fun toTop() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(0, 0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(scrollState?.layoutInfo?.totalItemsCount ?: 0, 0)
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
            val faviconApplier = FaviconApplier(context)
            withContext(Dispatchers.IO) {
                ExportedFileParser()(inputStream)
                    .map {
                        it.favicon = faviconApplier.makePath(it.url)
                        it
                    }
                    .forEach { bookmarkRepository.add(it) }
            }

            showRoot()
            contentViewModel?.snackShort(R.string.done_addition)
        }
    }

    /**
     * Show root folder.
     */
    private fun showRoot() {
        findByFolderName(Bookmark.getRootFolderName())
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

}