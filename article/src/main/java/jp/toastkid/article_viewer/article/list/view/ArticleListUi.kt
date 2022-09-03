/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModelFactory
import jp.toastkid.article_viewer.article.list.SearchResult
import jp.toastkid.article_viewer.article.list.date.DateFilterDialogUi
import jp.toastkid.article_viewer.article.list.menu.ArticleListMenuPopupActionUseCase
import jp.toastkid.article_viewer.article.list.menu.MenuPopupActionUseCase
import jp.toastkid.article_viewer.article.list.sort.SortSettingDialogUi
import jp.toastkid.article_viewer.article.list.usecase.UpdateUseCase
import jp.toastkid.article_viewer.calendar.DateSelectedActionUseCase
import jp.toastkid.article_viewer.zip.ZipFileChooserIntentFactory
import jp.toastkid.article_viewer.zip.ZipLoadProgressBroadcastIntentFactory
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleListUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val dataBase = AppDatabase.find(context)

    val articleRepository = dataBase.articleRepository()

    val preferenceApplier = PreferenceApplier(context)

    val bookmarkRepository = AppDatabase.find(context).bookmarkRepository()

    val contentViewModel = ViewModelProvider(context).get(ContentViewModel::class.java)

    val viewModel = ArticleListFragmentViewModelFactory(
        articleRepository,
        bookmarkRepository,
        preferenceApplier
    )
        .create(ArticleListFragmentViewModel::class.java)

    val progressBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            viewModel?.hideProgress()
            showFeedback()
        }

        private fun showFeedback() {
            contentViewModel?.snackShort(R.string.message_done_import)
        }
    }

    context.registerReceiver(
        progressBroadcastReceiver,
        ZipLoadProgressBroadcastIntentFactory.makeProgressBroadcastIntentFilter()
    )

    contentViewModel.replaceAppBarContent {
            AppBarContent(viewModel)
            val openSortDialog = remember { mutableStateOf(false) }

            if (openSortDialog.value) {
                SortSettingDialogUi(preferenceApplier, openSortDialog, onSelect = {
                    viewModel?.sort(it)
                })
            }

            val openDateDialog = remember { mutableStateOf(false) }

            if (openDateDialog.value) {
                DateFilterDialogUi(
                    preferenceApplier.colorPair(),
                    openDateDialog,
                    DateSelectedActionUseCase(articleRepository, contentViewModel)
                )
            }
        }

    val itemFlowState = remember { mutableStateOf<Flow<PagingData<SearchResult>>?>(null) }

    viewModel.dataSource.observe(context, {
        itemFlowState.value = it.flow
    })

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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        ArticleListUi(
            itemFlowState.value,
            rememberLazyListState(),
            contentViewModel,
            menuPopupUseCase,
            preferenceApplier.color
        )

        if (viewModel.progressVisibility.value) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
        }
    }

    LaunchedEffect(key1 = "first_launch", block = {
        viewModel.search("")
    })
}

@Composable
private fun AppBarContent(viewModel: ArticleListFragmentViewModel) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(activityContext)

    var searchInput by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf("") }
    Row {
        Column {
            TextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    CoroutineScope(Dispatchers.Default).launch {
                        //inputChannel.send(it)
                    }
                },
                label = {
                    Text(
                        stringResource(id = R.string.hint_search_articles),
                        color = MaterialTheme.colors.onPrimary
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions{
                    viewModel?.search(searchInput)
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color(preferenceApplier.fontColor),
                    cursorColor = MaterialTheme.colors.onPrimary
                ),
                trailingIcon = {
                    Icon(
                    Icons.Filled.Clear,
                    tint = Color(preferenceApplier.fontColor),
                    contentDescription = "clear text",
                    modifier = Modifier
                        .offset(x = 8.dp)
                        .clickable {
                            searchInput = ""
                        }
                )
                }
            )
            Text(text = searchResult, color = Color.White)
        }
    }

    viewModel?.progress?.observe(activityContext, {
        it?.getContentIfNotHandled()?.let { message ->
            searchResult = message
        }
    })
    viewModel?.messageId?.observe(activityContext, {
        it?.getContentIfNotHandled()?.let { messageId ->
            searchResult = activityContext.getString(messageId)
        }
    })

    val setTargetLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            UpdateUseCase(viewModel) { activityContext }.invokeIfNeed(it.data?.data)
        }

    val useTitleFilter = remember { mutableStateOf(preferenceApplier.useTitleFilter()) }
    val openSortDialog = remember { mutableStateOf(false) }
    val openDateDialog = remember { mutableStateOf(false) }

    val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
    contentViewModel.optionMenus(
        OptionMenu(
            titleId = R.string.action_all_article,
            action = {
                viewModel.search("")
            }
        ),
        OptionMenu(
            titleId = R.string.action_set_target,
            action = {
                setTargetLauncher.launch(ZipFileChooserIntentFactory()())
            }
        ),
        OptionMenu(
            titleId = R.string.action_sort,
            action = {
                openSortDialog.value = true
            }
        ),
        OptionMenu(
            titleId = R.string.action_date_filter,
            action = {
                openDateDialog.value = true
            }
        ),
        OptionMenu(
            titleId = R.string.action_switch_title_filter,
            action = {
                val newState = !useTitleFilter.value
                preferenceApplier.switchUseTitleFilter(newState)
                useTitleFilter.value = newState
            },
            checkState = useTitleFilter
        )
    )

    if (openSortDialog.value) {
        SortSettingDialogUi(preferenceApplier, openSortDialog, onSelect = {
            viewModel.sort(it)
        })
    }


    if (openDateDialog.value) {
        DateFilterDialogUi(
            preferenceApplier.colorPair(),
            openDateDialog,
            DateSelectedActionUseCase(AppDatabase.find(activityContext).articleRepository(), contentViewModel)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArticleListUi(
    flow: Flow<PagingData<SearchResult>>?,
    listState: LazyListState,
    contentViewModel: ContentViewModel?,
    menuPopupUseCase: MenuPopupActionUseCase,
    @ColorInt menuIconColor: Int
) {
    val articles = flow?.collectAsLazyPagingItems() ?: return

    LazyColumn(state = listState) {
        items(articles, { it.id }) {
            it ?: return@items
            ListItem(it, contentViewModel, menuPopupUseCase, menuIconColor)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    ScrollerUseCase(contentViewModel, listState).invoke(lifecycleOwner)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListItem(
    article: SearchResult,
    contentViewModel: ContentViewModel?,
    menuPopupUseCase: MenuPopupActionUseCase,
    @ColorInt menuIconColor: Int
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        stringResource(id = R.string.action_add_to_bookmark),
        stringResource(id = R.string.delete)
    )

    Surface(
        elevation = 4.dp,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
            .combinedClickable(
                onClick = { contentViewModel?.newArticle(article.title) },
                onLongClick = { contentViewModel?.newArticleOnBackground(article.title) }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = "Last updated: ${
                        DateFormat.format(
                            "yyyy/MM/dd(E) HH:mm:ss",
                            article.lastModified
                        )
                    }" +
                            " / ${article.length}",
                    maxLines = 1,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box(
                Modifier
                    .width(32.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    R.drawable.ic_more,
                    stringResource(id = R.string.menu),
                    colorFilter = ColorFilter.tint(
                        Color(menuIconColor),
                        BlendMode.SrcIn
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            expanded = true
                        }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(onClick = {
                            when (index) {
                                0 -> menuPopupUseCase.addToBookmark(article.id)
                                1 -> menuPopupUseCase.delete(article.id)
                            }
                            expanded = false
                        }) { Text(text = s) }
                    }
                }
            }
        }
    }
}