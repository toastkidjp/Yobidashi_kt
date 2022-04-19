package jp.toastkid.yobidashi.browser.history

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.view.BindItemContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ViewHistoryFragment: Fragment(), ContentScrollable {

    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    private var scrollState: LazyListState? = null

    private val itemState: SnapshotStateList<ViewHistory> = mutableStateListOf()

    private var viewHistoryRepository: ViewHistoryRepository? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = activity ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        contentViewModel = activity?.let { ViewModelProvider(it).get(ContentViewModel::class.java) }

        setHasOptionsMenu(true)

        return ComposeViewFactory().invoke(context) {
            val database = DatabaseFinder().invoke(LocalContext.current)
            val viewHistoryRepository = database.viewHistoryRepository()
            this.viewHistoryRepository = viewHistoryRepository
            val viewHistoryItems = remember { itemState }

            val coroutineScope = rememberCoroutineScope()

            val listState = rememberLazyListState()
            this.scrollState = listState

            MaterialTheme() {
                Surface(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .background(colorResource(id = R.color.setting_background))
                            .nestedScroll(rememberViewInteropNestedScrollConnection())
                    ) {
                        coroutineScope.launch {
                            val loaded = withContext(Dispatchers.IO) {
                                viewHistoryRepository.reversed()
                            }
                            viewHistoryItems.clear()
                            viewHistoryItems.addAll(loaded)
                        }

                        items(viewHistoryItems) { viewHistory ->
                            BindItemContent(
                                viewHistory,
                                onClick = {
                                    finishWithResult(Uri.parse(viewHistory.url))
                                },
                                onLongClick = {
                                    val browserViewModel =
                                        ViewModelProvider(context).get(BrowserViewModel::class.java)
                                    browserViewModel.openBackground(viewHistory.title, Uri.parse(viewHistory.url))

                                },
                                onDelete = {
                                    viewHistoryRepository.delete(viewHistory)
                                    itemState.remove(viewHistory)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentActivity = activity ?: return
        ViewModelProvider(fragmentActivity).get(PageSearcherViewModel::class.java)
                .find
                .observe(viewLifecycleOwner, Observer {
                    filter(it)
                })

        parentFragmentManager.setFragmentResultListener(
            "clear_items",
            viewLifecycleOwner,
            { _, _ ->
                clearAll{ contentViewModel?.snackShort(R.string.done_clear)}
                popBackStack()
            }
        )
    }

    private fun filter(query: String?) {
        if (query.isNullOrBlank()) {
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val searchResult = withContext(Dispatchers.IO) {
                viewHistoryRepository?.search("%$query%")
            } ?: return@launch

            itemState.clear()
            itemState.addAll(searchResult)
        }
    }

    private fun clearAll(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                viewHistoryRepository?.deleteAll()
            }

            onComplete()

            contentViewModel?.snackShort(R.string.message_none_search_histories)
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun finishWithResult(uri: Uri?) {
        if (uri == null) {
            return
        }

        val activity = activity ?: return
        val browserViewModel =
                ViewModelProvider(activity).get(BrowserViewModel::class.java)

        popBackStack()
        browserViewModel.open(uri)
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.view_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                ConfirmDialogFragment.show(
                    parentFragmentManager,
                    getString(R.string.title_clear_view_history),
                    Html.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    ),
                    "clear_items"
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    override fun onDetach() {
        parentFragmentManager.clearFragmentResultListener("clear_items")

        super.onDetach()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_view_history

    }
}