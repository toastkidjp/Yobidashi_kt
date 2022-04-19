package jp.toastkid.yobidashi.search.favorite

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.favorite.usecase.ClearItemsUseCase
import jp.toastkid.yobidashi.search.view.SearchCategorySpinner
import jp.toastkid.yobidashi.search.view.SearchItemContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.MessageFormat

/**
 * Favorite search fragment.
 *
 * @author toastkidjp
 */
class FavoriteSearchFragment : Fragment(), CommonFragmentAction {

    private lateinit var preferenceApplier: PreferenceApplier

    private var scrollState: LazyListState? = null

    private var editorOpen: MutableState<Boolean>? = null

    private val favoriteSearchItems = mutableStateListOf<FavoriteSearch>()

    private val disposables: Job by lazy { Job() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityContext = activity
            ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        setHasOptionsMenu(true)

        val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

        ViewModelProvider(activityContext).get(AppBarViewModel::class.java)
            .replace(activityContext) {
                val editorOpen = remember { mutableStateOf(false) }
                this.editorOpen = editorOpen

                Button(
                    onClick = { editorOpen.value = true },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = colorResource(id = R.color.soft_background),
                        contentColor = colorResource(id = R.color.colorPrimary),
                        disabledContentColor = Color.LightGray
                    )
                ) {
                    Text(text = stringResource(id = R.string.add))

                    val spinnerOpen = remember { mutableStateOf(false) }
                    val categoryName = remember {
                        mutableStateOf(
                            PreferenceApplier(activityContext).getDefaultSearchEngine()
                                ?: SearchCategory.getDefaultCategoryName()
                        )
                    }
                    val input = remember { mutableStateOf("") }

                    Popup {
                        Column(modifier = Modifier
                            .background(colorResource(id = R.color.soft_background))
                            .padding(dimensionResource(id = R.dimen.settings_item_left_margin))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(id = R.string.category),
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                SearchCategorySpinner(spinnerOpen, categoryName)
                            }
                            // TODO imeActionLabel="@string/title_add"
                            TextField(
                                value = input.value,
                                onValueChange = { input.value = it },
                                label = { stringResource(id = R.string.word) }
                            )

                            Button(
                                onClick = {
                                    if (input.value.isEmpty()) {
                                        contentViewModel.snackShort(
                                            R.string.favorite_search_addition_dialog_empty_message
                                        )
                                        return@Button
                                    }

                                    FavoriteSearchInsertion(activityContext, categoryName.value, input.value).invoke()

                                    reload(DatabaseFinder().invoke(activityContext).favoriteSearchRepository())

                                    val message = MessageFormat.format(
                                        getString(R.string.favorite_search_addition_successful_format),
                                        input.value
                                    )
                                    contentViewModel.snackShort(message)
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    backgroundColor = Color(preferenceApplier.color),
                                    contentColor = Color(preferenceApplier.fontColor),
                                    disabledContentColor = Color.LightGray
                                ),
                                modifier = Modifier.fillMaxWidth().padding(4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.title_add)
                                )
                            }
                        }
                    }
                }
            }

        return ComposeViewFactory().invoke(activityContext) {
            FavoriteSearchItemListUi()
        }
    }

    @Composable
    fun FavoriteSearchItemListUi() {
        val context = LocalContext.current
        val database = DatabaseFinder().invoke(context)
        val repository = database.favoriteSearchRepository()

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
                    reload(repository)

                    items(favoriteSearchItems) { favoriteSearch ->
                        SearchItemContent(
                            favoriteSearch.query,
                            favoriteSearch.category,
                            {
                                SearchAction(
                                    context,
                                    favoriteSearch.category ?: "",
                                    favoriteSearch.query ?: "",
                                    onBackground = it
                                ).invoke()
                                startSearch(SearchCategory.findByCategory(favoriteSearch.category), favoriteSearch.query ?: "")
                            },
                            {
                                repository.delete(favoriteSearch)
                                favoriteSearchItems.remove(favoriteSearch)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun reload(repository: FavoriteSearchRepository) {
        CoroutineScope(Dispatchers.Main).launch {
            val loaded = withContext(Dispatchers.IO) {
                repository.findAll()
            }
            favoriteSearchItems.clear()
            favoriteSearchItems.addAll(loaded)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "clear_favorite_search_items",
            this,
            { _, _ -> clear() }
        )
    }

    /**
     * Start search action.
     *
     * @param category Search category
     * @param query    Search query
     */
    private fun startSearch(category: SearchCategory, query: String) {
        activity?.let {
            SearchAction(it, category.name, query).invoke()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.favorite_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.favorite_toolbar_menu_clear -> {
                    ConfirmDialogFragment.show(
                        parentFragmentManager,
                        getString(R.string.title_delete_all),
                        Html.fromHtml(
                            getString(R.string.confirm_clear_all_settings),
                            Html.FROM_HTML_MODE_COMPACT
                        ),
                        "clear_favorite_search_items"
                    )
                    true
                }
                R.id.favorite_toolbar_menu_add -> {
                    // TODO invokeAddition()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun clear() {
        val showSnackbar: (Int) -> Unit = { messageId ->
            activity?.let { activity ->
                ViewModelProvider(activity).get(ContentViewModel::class.java)
                    .snackShort(messageId)
            }
        }
        ClearItemsUseCase(showSnackbar).invoke(activity, disposables)
    }

    /**
     * Implement for called from Data-Binding.
     */
    fun add() {
        // TODO Delete it.
    }

    override fun pressBack(): Boolean {
        if (editorOpen?.value == true) {
            editorOpen?.value = false
            return true
        }
        return super.pressBack()
    }

    override fun onDetach() {
        parentFragmentManager.clearFragmentResultListener("clear_favorite_search_items")
        disposables.cancel()
        super.onDetach()
    }

}