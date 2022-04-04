/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.trend.Trend
import jp.toastkid.yobidashi.search.trend.TrendApi
import jp.toastkid.yobidashi.search.url_suggestion.ItemDeletionUseCase
import jp.toastkid.yobidashi.search.viewmodel.SearchUiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun SearchContentsUi(
    viewModel: SearchUiViewModel,
    input: State<String>?,
    currentTitle: String?,
    currentUrl: String?
) {
    val preferenceApplier = PreferenceApplier(LocalContext.current)

    val query = remember { input }

    val database = DatabaseFinder().invoke(LocalContext.current)
    val bookmarkRepository: BookmarkRepository = database.bookmarkRepository()
    val viewHistoryRepository: ViewHistoryRepository = database.viewHistoryRepository()
    val favoriteSearchRepository = database.favoriteSearchRepository()
    val searchHistoryRepository = database.searchHistoryRepository()

    val itemDeletionUseCase = ItemDeletionUseCase(bookmarkRepository, viewHistoryRepository)

    val fullContentUri =
        "https://trends.google.co.jp/trends/trendingsearches/realtime"

    val trendApi = TrendApi()

    val trends = remember { mutableListOf<Trend>() }

    LaunchedEffect(
        key1 = "",
        block = {
            CoroutineScope(Dispatchers.IO).launch {
                val trendItems = try {
                    trendApi()
                } catch (e: IOException) {
                    Timber.e(e)
                    null
                }
                trends.clear()
                val taken = trendItems?.take(10)
                if (taken.isNullOrEmpty()) {
                    return@launch
                }
                trends.addAll(taken)
            }
        }
    )

    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp),
        modifier = Modifier
            .nestedScroll(rememberViewInteropNestedScrollConnection())
    ) {
        if (preferenceApplier.isEnableUrlModule()) {
            item {
                UrlCard(currentTitle, currentUrl)
            }
        }

        if (viewModel.urlItems.isNotEmpty()) {
            item {
                HeaderWithLink(R.string.title_view_history, R.string.link_open_history) {
                    //TODO module.openHistory()
                }
            }

            items(viewModel.urlItems, { it.itemId() }) { urlItem ->
                BindItemContent(
                    urlItem,
                    onClick = {
                        viewModel.search(urlItem.urlString())
                    },
                    onLongClick = {
                        viewModel.searchOnBackground(urlItem.urlString())
                    },
                    onDelete = {
                        itemDeletionUseCase(urlItem)
                        viewModel.urlItems.remove(urlItem)
                    }
                )
            }
        }

        if (viewModel.favoriteSearchItems.isNotEmpty()) {
            item {
                Header(R.string.title_favorite_search)
            }

            items(viewModel.favoriteSearchItems.take(5)) { favoriteSearch ->
                SearchItemContent(
                    favoriteSearch.query,
                    favoriteSearch.category,
                    {
                        viewModel.searchWithCategory(
                            favoriteSearch.query ?: "",
                            favoriteSearch.category ?: "",
                            it
                        )
                    },
                    {
                        favoriteSearchRepository.delete(favoriteSearch)
                        viewModel.favoriteSearchItems.remove(favoriteSearch)
                    }
                )
            }
        }

        if (viewModel.searchHistories.isNotEmpty()) {
            item {
                Header(R.string.title_search_history)
            }

            items(viewModel.searchHistories.take(5)) { searchHistory ->
                SearchItemContent(
                    searchHistory.query,
                    searchHistory.category,
                    {
                        viewModel.searchWithCategory(
                            searchHistory.query ?: "",
                            searchHistory.category ?: "",
                            it
                        )
                    },
                    {
                        searchHistoryRepository.delete(searchHistory)
                        viewModel.searchHistories.remove(searchHistory)
                    },
                    searchHistory.timestamp
                )
            }
        }

        if (viewModel.suggestions.isNotEmpty()) {
            item {
                Header(R.string.title_search_suggestion)
            }
        }

        item {
            FlowRow(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                viewModel.suggestions.take(10).forEach {
                    ItemCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.combinedClickable(
                                true,
                                onClick = {
                                    viewModel?.putQuery(it)
                                    viewModel?.search(it)
                                },
                                onLongClick = {
                                    viewModel?.searchOnBackground(it)
                                }
                            )
                        ) {
                            Text(
                                text = it,
                                color = colorResource(id = R.color.black),
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .wrapContentWidth()
                            )
                            Text(
                                text = stringResource(id = R.string.plus),
                                color = colorResource(id = R.color.white),
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(32.dp)
                                    .background(colorResource(id = R.color.pre4_ripple))
                                    .clickable { viewModel?.putQuery("$it ") }
                            )
                        }
                    }
                }
            }
        }

        if (preferenceApplier.isEnableTrendModule()) {
            item {
                if (trends.isEmpty()) {
                    return@item
                }
                HeaderWithLink(R.string.hourly_trends, R.string.open) {
                    viewModel?.search(fullContentUri)
                }
            }

            item {
                FlowRow(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                    trends.take(10).forEach {
                        ItemCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.combinedClickable(
                                    true,
                                    onClick = {
                                        viewModel?.search(it.link)
                                    },
                                    onLongClick = {
                                        viewModel?.searchOnBackground(it.link)
                                    }
                                )
                            ) {
                                AsyncImage(
                                    model = it.image,
                                    contentDescription = it.title,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    text = it.title,
                                    color = colorResource(id = R.color.black),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .padding(start = 4.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.plus),
                                    color = colorResource(id = R.color.white),
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(32.dp)
                                        .background(colorResource(id = R.color.pre4_ripple))
                                        .clickable { viewModel?.putQuery("${it.title} ") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlCard(currentTitle: String?, currentUrl: String?) {
    if (currentUrl.isNullOrBlank() && currentUrl.isNullOrBlank()) {
        return
    }

    val color = IconColorFinder.from(LocalContext.current).invoke()

    Surface(
        elevation = 4.dp,
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTitle ?: "",
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
                Text(
                    text = currentUrl ?: "",
                    color = colorResource(id = R.color.link_blue),
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
            }
            Image(
                painterResource(id = R.drawable.ic_share_black),
                contentDescription = stringResource(id = R.string.share),
                colorFilter = ColorFilter.tint(Color(color), BlendMode.SrcIn),
                modifier = Modifier
                    .width(32.dp)
                    .clickable { /* TODO shareUrl */ }
            )
            Image(
                painterResource(id = R.drawable.ic_clip),
                contentDescription = stringResource(id = R.string.clip),
                colorFilter = ColorFilter.tint(Color(color), BlendMode.SrcIn),
                modifier = Modifier
                    .width(32.dp)
                    .clickable { /* TODO clipUrl */ }
            )
            Image(
                painterResource(id = R.drawable.ic_edit_black),
                contentDescription = stringResource(id = R.string.edit),
                colorFilter = ColorFilter.tint(Color(color), BlendMode.SrcIn),
                modifier = Modifier
                    .width(32.dp)
                    .clickable { /* TODO edit */ }
            )
        }
    }
}

@Composable
private fun Header(headerTextId: Int) {
    Text(
        text = stringResource(id = headerTextId),
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .background(colorResource(id = R.color.soft_background))
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
private fun HeaderWithLink(headerTextId: Int, linkTextId: Int, onLinkClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(start = 8.dp, end = 8.dp)
        .background(colorResource(id = R.color.soft_background))
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        Text(
            stringResource(id = headerTextId),
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
        Text(
            stringResource(id = linkTextId),
            color = colorResource(id = R.color.link_blue),
            modifier = Modifier
                .clickable(onClick = onLinkClick)
                .align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun ItemCard(content: @Composable () -> Unit) {
    Surface(
        elevation = 4.dp,
        content = content,
        modifier = Modifier.padding(2.dp)
    )
}