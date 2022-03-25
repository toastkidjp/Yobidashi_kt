/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.view

import android.text.format.DateFormat
import androidx.annotation.ColorInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.list.SearchResult
import jp.toastkid.article_viewer.article.list.menu.MenuPopupActionUseCase
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import kotlinx.coroutines.flow.Flow

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

    MaterialTheme {
        LazyColumn(
            state = listState,
            modifier = Modifier.nestedScroll(rememberViewInteropNestedScrollConnection())
        ) {
            items(articles, { it.id }) {
                it ?: return@items
                ListItem(it, contentViewModel, menuPopupUseCase, menuIconColor)
            }
        }
    }
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
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
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
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(colorResource(id = R.color.soft_background))
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