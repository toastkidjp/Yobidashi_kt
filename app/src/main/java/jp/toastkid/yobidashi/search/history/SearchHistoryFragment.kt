package jp.toastkid.yobidashi.search.history

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.history.usecase.ClearItemsUseCase
import jp.toastkid.yobidashi.search.view.SearchItemContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryFragment : Fragment(), ContentScrollable {

    private lateinit var preferenceApplier: PreferenceApplier

    private var scrollState: LazyListState? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        setHasOptionsMenu(true)

        parentFragmentManager.setFragmentResultListener(
            "clear_search_history_items",
            viewLifecycleOwner,
            { _, _ ->
                ClearItemsUseCase({
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            DatabaseFinder().invoke(context).searchHistoryRepository().deleteAll()
                        }

                        view?.let { parent ->
                            Toaster.snackShort(
                                parent,
                                R.string.settings_color_delete,
                                preferenceApplier.colorPair()
                            )
                        }

                        activity?.supportFragmentManager?.popBackStack()
                    }
                }).invoke(activity)
            }
        )

        return ComposeViewFactory().invoke(context) {
            val database = DatabaseFinder().invoke(LocalContext.current)
            val searchHistoryRepository = database.searchHistoryRepository()
            val searchHistoryItems = remember { mutableStateListOf<SearchHistory>() }

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
                                searchHistoryRepository.findAll()
                            }
                            searchHistoryItems.clear()
                            searchHistoryItems.addAll(loaded)
                        }

                        items(searchHistoryItems) { searchHistory ->
                            SearchItemContent(
                                searchHistory.query,
                                searchHistory.category,
                                {
                                    SearchAction(
                                        context,
                                        searchHistory.category ?: "",
                                        searchHistory.query ?: "",
                                        onBackground = it
                                    ).invoke()
                                },
                                {
                                    searchHistoryRepository.delete(searchHistory)
                                    searchHistoryItems.remove(searchHistory)
                                },
                                searchHistory.timestamp
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                ConfirmDialogFragment.show(
                    parentFragmentManager,
                    getString(R.string.title_clear_search_history),
                    Html.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    ),
                    "clear_search_history_items"
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        parentFragmentManager.clearFragmentResultListener("clear_search_history_items")
        super.onDetach()
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

}