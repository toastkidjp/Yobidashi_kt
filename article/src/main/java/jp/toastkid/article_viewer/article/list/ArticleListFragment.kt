/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.date.DateFilterDialogFragment
import jp.toastkid.article_viewer.article.list.date.FilterByMonthUseCase
import jp.toastkid.article_viewer.article.list.menu.ArticleListMenuPopupActionUseCase
import jp.toastkid.article_viewer.article.list.sort.Sort
import jp.toastkid.article_viewer.article.list.sort.SortSettingDialogFragment
import jp.toastkid.article_viewer.article.list.usecase.UpdateUseCase
import jp.toastkid.article_viewer.article.list.view.ArticleListUi
import jp.toastkid.article_viewer.bookmark.BookmarkFragment
import jp.toastkid.article_viewer.zip.ZipFileChooserIntentFactory
import jp.toastkid.article_viewer.zip.ZipLoadProgressBroadcastIntentFactory
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Article list fragment.
 *
 * @author toastkidjp
 */
class ArticleListFragment : Fragment(), ContentScrollable, OnBackCloseableTabUiFragment {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferencesWrapper: PreferenceApplier

    /**
     * Use for read articles from DB.
     */
    private lateinit var articleRepository: ArticleRepository

    /**
     * Use for receiving broadcast.
     */
    private val progressBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            viewModel?.hideProgress()
            showFeedback()
        }

        private fun showFeedback() {
            contentViewModel?.snackShort(R.string.message_done_import)
        }
    }

    private var contentViewModel: ContentViewModel? = null

    private var viewModel: ArticleListFragmentViewModel? = null

    private val inputChannel = Channel<String>()

    private val setTargetLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            UpdateUseCase(viewModel) { context }.invokeIfNeed(it.data?.data)
        }

    private var scrollState: LazyListState? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)

        initializeRepository(context)

        preferencesWrapper = PreferenceApplier(context)

        context.registerReceiver(
            progressBroadcastReceiver,
            ZipLoadProgressBroadcastIntentFactory.makeProgressBroadcastIntentFilter()
        )

        setHasOptionsMenu(true)

        val bookmarkRepository = AppDatabase.find(context).bookmarkRepository()

        viewModel = ArticleListFragmentViewModelFactory(
            articleRepository,
            bookmarkRepository,
            preferencesWrapper
        )
            .create(ArticleListFragmentViewModel::class.java)

        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        activity?.let {
            val appBarComposeView = ComposeView(context)
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            appBarComposeView.setContent {
                AppBarContent()
            }
            ViewModelProvider(it).get(AppBarViewModel::class.java)
                .replace(appBarComposeView)
            CoroutineScope(Dispatchers.IO).launch {
                viewModel?.search("")
            }
        }

        val menuPopupUseCase = ArticleListMenuPopupActionUseCase(
            articleRepository,
            bookmarkRepository,
            {
                contentViewModel?.snackWithAction(
                    "Deleted: \"${it.title}\".",
                    "UNDO"
                ) { CoroutineScope(Dispatchers.IO).launch { articleRepository.insert(it) } }
            }
        )

        viewModel?.dataSource?.observe(viewLifecycleOwner, {
            composeView.setContent {
                val listState = rememberLazyListState()
                this.scrollState = listState
                ArticleListUi(
                    it?.flow,
                    listState,
                    contentViewModel,
                    menuPopupUseCase,
                    preferencesWrapper.color
                )
            }
        })

        CoroutineScope(Dispatchers.Default).launch {
            inputChannel.receiveAsFlow()
                .distinctUntilChanged()
                .debounce(1400L)
                .collect {
                    withContext(Dispatchers.Main) {
                        //viewModel?.filter(it)
                    }
                }
        }

        return composeView
    }

    @Composable
    fun AppBarContent() {
        val activityContext = activity ?: return

        contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

        var searchInput by remember { mutableStateOf("") }
        var searchResult by remember { mutableStateOf("") }
        Row {
            Column {
                TextField(
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                        CoroutineScope(Dispatchers.Default).launch {
                            inputChannel.send(it)
                        }
                                    },
                    label = { stringResource(id = R.string.hint_search_articles) },
                    singleLine = true,
                    keyboardActions = KeyboardActions{
                        viewModel?.search(searchInput)
                    },
                    colors = TextFieldDefaults.textFieldColors(textColor = Color(preferencesWrapper.fontColor)),
                    trailingIcon = {Icon(
                        Icons.Filled.Clear,
                        tint = Color(preferencesWrapper.fontColor),
                        contentDescription = "clear text",
                        modifier = Modifier
                            .offset(x = 8.dp)
                            .clickable {
                                searchInput = ""
                            }
                    )}
                )
                Text(text = searchResult, color = Color.White)
            }
        }

        viewModel?.progressVisibility?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { isVisible ->
                //TODO binding.progressCircular.isVisible = isVisible
            }
        })
        viewModel?.progress?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { message ->
                searchResult = message
            }
        })
        viewModel?.messageId?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { messageId ->
                searchResult = activityContext.getString(messageId)
            }
        })

        parentFragmentManager.setFragmentResultListener(
            "sorting",
            viewLifecycleOwner,
            { key, result ->
                val sort = result[key] as? Sort ?: return@setFragmentResultListener
                viewModel?.sort(sort)
            }
        )

        parentFragmentManager.setFragmentResultListener(
            "date_filter",
            viewLifecycleOwner,
            { _, result ->
                val year = result.getInt("year")
                val month = result.getInt("month")
                FilterByMonthUseCase(
                    ViewModelProvider(this).get(ArticleListFragmentViewModel::class.java)
                ).invoke(year, month)
            }
        )
    }

    private fun initializeRepository(activityContext: Context) {
        val dataBase = AppDatabase.find(activityContext)

        articleRepository = dataBase.articleRepository()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_article_list, menu)
        menu.findItem(R.id.action_switch_title_filter)?.isChecked =
            preferencesWrapper.useTitleFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_all_article -> {
                activity?.let {
                    viewModel?.search("")
                }
                true
            }
            R.id.action_bookmark -> {
                contentViewModel?.nextFragment(BookmarkFragment::class.java)
                true
            }
            R.id.action_set_target -> {
                setTargetLauncher.launch(ZipFileChooserIntentFactory()())
                true
            }
            R.id.action_sort -> {
                val dialogFragment = SortSettingDialogFragment()
                dialogFragment.show(parentFragmentManager, "")
                true
            }
            R.id.action_date_filter -> {
                val dateFilterDialogFragment = DateFilterDialogFragment()
                dateFilterDialogFragment.show(parentFragmentManager, "")
                true
            }
            R.id.action_switch_title_filter -> {
                val newState = !item.isChecked
                preferencesWrapper.switchUseTitleFilter(newState)
                item.isChecked = newState
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
        inputChannel.cancel()
        context?.unregisterReceiver(progressBroadcastReceiver)
        setTargetLauncher.unregister()
        parentFragmentManager.clearFragmentResultListener("sorting")
        parentFragmentManager.clearFragmentResultListener("date_filter")
        super.onDetach()
    }

}