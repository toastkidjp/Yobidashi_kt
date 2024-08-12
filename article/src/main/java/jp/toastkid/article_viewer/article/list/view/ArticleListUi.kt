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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.ArticleRepositoryFactory
import jp.toastkid.article_viewer.article.data.BookmarkRepositoryFactory
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
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
import kotlinx.coroutines.launch

@Composable
fun ArticleListUi() {
    val context = LocalContext.current as? ComponentActivity ?: return

    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val viewModel = remember {
        ArticleListFragmentViewModel(
            ArticleRepositoryFactory().invoke(context),
            BookmarkRepositoryFactory().invoke(context),
            PreferenceApplier(context)
        )
    }

    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        contentViewModel.replaceAppBarContent {
            AppBarContent(viewModel)
            val openSortDialog = remember { mutableStateOf(false) }

            if (openSortDialog.value) {
                SortSettingDialogUi(PreferenceApplier(context), { openSortDialog.value = false }, onSelect = {
                    viewModel.sort(it)
                })
            }

            val openDateDialog = remember { mutableStateOf(false) }

            if (openDateDialog.value) {
                DateFilterDialogUi(
                    { openDateDialog.value = false },
                    DateSelectedActionUseCase(ArticleRepositoryFactory().invoke(context), contentViewModel)
                )
            }
        }
    })

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        ArticleListUi(
            viewModel.dataSource().collectAsLazyPagingItems(),
            contentViewModel,
            ArticleListMenuPopupActionUseCase(
                ArticleRepositoryFactory().invoke(context),
                BookmarkRepositoryFactory().invoke(context),
                {
                    contentViewModel.snackWithAction(
                        "Deleted: \"${it.title}\".",
                        "UNDO"
                    ) { CoroutineScope(Dispatchers.IO).launch { ArticleRepositoryFactory().invoke(context).insert(it) } }
                }
            )
        )

        if (viewModel.progressVisibility()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    LaunchedEffect(key1 = "first_launch", block = {
        viewModel.search("")
    })

    DisposableEffect(key1 = "unregisterReceiver", effect = {
        val progressBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModel.hideProgress()
                showFeedback()
            }

            private fun showFeedback() {
                contentViewModel.snackShort(R.string.message_done_import)
            }
        }

        context.registerReceiver(
            progressBroadcastReceiver,
            ZipLoadProgressBroadcastIntentFactory.makeProgressBroadcastIntentFilter()
        )
        onDispose {
            context.unregisterReceiver(progressBroadcastReceiver)
        }
    })
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AppBarContent(viewModel: ArticleListFragmentViewModel) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = remember { ViewModelProvider(activityContext).get<ContentViewModel>() }
    val cursorColor = remember { Color(PreferenceApplier(activityContext).editorCursorColor(Color(0xFFE0E0E0).toArgb())) }

    Row {
        Column(Modifier.weight(1f)) {
            TextField(
                value = viewModel.searchInput(),
                onValueChange = {
                    viewModel.setSearchInput(it)

                    if (PreferenceApplier(activityContext).useTitleFilter()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.filter("%$it%")
                        }
                    }
                },
                label = {
                    Text(
                        stringResource(id = R.string.hint_search_articles),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions{
                    viewModel.search(viewModel.searchInput())
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    imeAction = ImeAction.Search
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    cursorColor = cursorColor
                ),
                trailingIcon = {
                    Icon(
                        Icons.Filled.Clear,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "clear text",
                        modifier = Modifier
                            .offset(x = 8.dp)
                            .clickable {
                                viewModel.setSearchInput("")
                            }
                    )
                },
                modifier = Modifier.weight(0.7f)
            )
            Text(
                text = viewModel.searchResult(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(0.3f)
                    .padding(start = 16.dp)
            )
        }

        Box(
            Modifier
                .width(40.dp)
                .fillMaxHeight()
                .combinedClickable(
                    true,
                    onClick = {
                        contentViewModel.switchTabList()
                    },
                    onLongClick = {
                        contentViewModel.openNewTab()
                    }
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tab),
                contentDescription = stringResource(id = R.string.tab),
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onPrimary,
                    BlendMode.SrcIn
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp)
            )
            Text(
                text = contentViewModel.tabCount.value.toString(),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 2.dp, bottom = 2.dp)
            )
        }
    }

    val setTargetLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            UpdateUseCase(viewModel) { activityContext }.invokeIfNeed(it.data?.data)
        }

    val openSortDialog = remember { mutableStateOf(false) }
    val openDateDialog = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = "add_option_menu", block = {
        ViewModelProvider(activityContext).get(ContentViewModel::class.java).optionMenus(
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
                    PreferenceApplier(activityContext).switchUseTitleFilter()
                },
                check = { PreferenceApplier(activityContext).useTitleFilter() }
            )
        )
    })

    if (openSortDialog.value) {
        SortSettingDialogUi(PreferenceApplier(activityContext), { openSortDialog.value = false }, onSelect = {
            viewModel.sort(it)
        })
    }

    if (openDateDialog.value) {
        DateFilterDialogUi(
            { openDateDialog.value = false },
            DateSelectedActionUseCase(
                ArticleRepositoryFactory().invoke(activityContext),
                ViewModelProvider(activityContext).get(ContentViewModel::class.java)
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArticleListUi(
    articles: LazyPagingItems<SearchResult>,
    contentViewModel: ContentViewModel?,
    menuPopupUseCase: MenuPopupActionUseCase
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(state = lazyListState) {
        items(articles, { it.id }) {
            it ?: return@items
            ListItem(it, contentViewModel, menuPopupUseCase,
                Modifier.animateItemPlacement()
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    ScrollerUseCase(contentViewModel, lazyListState).invoke(lifecycleOwner)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListItem(
    article: SearchResult,
    contentViewModel: ContentViewModel?,
    menuPopupUseCase: MenuPopupActionUseCase,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        stringResource(id = R.string.action_add_to_bookmark),
        stringResource(id = R.string.delete)
    )

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier
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
                    text = stringResource(
                        R.string.label_last_modified,
                        DateFormat.format(
                            "yyyy/MM/dd(E) HH:mm:ss",
                            article.lastModified
                        ),
                        article.length
                    ),
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
                Icon(
                    painterResource(id = R.drawable.ic_more),
                    stringResource(id = R.string.menu),
                    tint = MaterialTheme.colorScheme.secondary,
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
                        DropdownMenuItem(
                            text = { Text(text = s) },
                            onClick = {
                            when (index) {
                                0 -> menuPopupUseCase.addToBookmark(article.id)
                                1 -> menuPopupUseCase.delete(article.id)
                            }
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}