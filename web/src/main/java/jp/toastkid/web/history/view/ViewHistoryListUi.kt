/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.history.view

import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.list.ListActionAttachment
import jp.toastkid.lib.view.list.SwipeToDismissItem
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.web.R
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.history.ViewHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ViewHistoryListUi() {
    val context = LocalContext.current as? ComponentActivity ?: return

    val viewHistoryRepository = remember { RepositoryFactory().viewHistoryRepository(context) }
    val fullItems = remember { mutableListOf<ViewHistory>() }
    val viewHistoryItems = remember { mutableStateListOf<ViewHistory>() }
    val listState = rememberLazyListState()

    val clearConfirmDialogState = remember { mutableStateOf(false) }
    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    LaunchedEffect(key1 = "first_launch", block = {
        contentViewModel.optionMenus(
            OptionMenu(titleId = R.string.title_clear_view_history, action = {
                clearConfirmDialogState.value = true
            })
        )

        CoroutineScope(Dispatchers.Main).launch {
            val loaded = withContext(Dispatchers.IO) {
                viewHistoryRepository.reversed()
            }
            fullItems.clear()
            fullItems.addAll(loaded)
            viewHistoryItems.addAll(fullItems)
        }
    })

    List(viewHistoryItems, listState, { viewHistory, isLongClick ->
        when (isLongClick) {
            true -> {
                contentViewModel.openBackground(viewHistory.title, Uri.parse(viewHistory.url))
            }
            false -> {
                contentViewModel.open(Uri.parse(viewHistory.url))
            }
        }
    }) {
        viewHistoryRepository.delete(it)
        viewHistoryItems.remove(it)
    }

    ListActionAttachment.make(context)
        .invoke(
            listState,
            LocalLifecycleOwner.current,
            viewHistoryItems,
            fullItems,
            { item, word -> item.title.contains(word) || item.url.contains(word) }
        )

    if (clearConfirmDialogState.value) {
        DestructiveChangeConfirmDialog(
            R.string.title_clear_view_history,
            onDismissRequest = { clearConfirmDialogState.value = false }
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    viewHistoryRepository.deleteAll()
                }
                viewHistoryItems.clear()

                contentViewModel.snackShort(jp.toastkid.lib.R.string.done_clear)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun List(
    viewHistoryItems: SnapshotStateList<ViewHistory>,
    listState: LazyListState,
    onClick: (ViewHistory, Boolean) -> Unit,
    onDelete: (ViewHistory) -> Unit
) {
    LazyColumn(state = listState) {
        items(viewHistoryItems, { it._id }) { viewHistory ->
            BindItemContent(
                viewHistory,
                onClick = {
                    onClick(viewHistory, false)
                },
                onLongClick = {
                    onClick(viewHistory, true)
                },
                onDelete = {
                    onDelete(viewHistory)
                },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BindItemContent(
    urlItem: UrlItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (urlItem) {
        is Bookmark -> urlItem.title
        is ViewHistory -> urlItem.title
        else -> ""
    }
    val url = when (urlItem) {
        is Bookmark -> urlItem.url
        is ViewHistory -> urlItem.url
        else -> ""
    }
    val time = dateFormat(
        when (urlItem) {
            is Bookmark -> urlItem.lastViewed
            is ViewHistory -> urlItem.lastViewed
            else -> 0L
        }
    )

    val iconFile = when (urlItem) {
        is Bookmark -> File(urlItem.favicon)
        is ViewHistory -> File(urlItem.favicon)
        else -> null
    }

    SwipeToDismissItem(
        onClickDelete = {
            CoroutineScope(Dispatchers.IO).launch {
                onDelete()
            }
        },
        dismissContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.combinedClickable(
                    true,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            ) {
                AsyncImage(
                    iconFile,
                    contentDescription = urlItem.urlString(),
                    placeholder = painterResource(id = jp.toastkid.lib.R.drawable.ic_history_black),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                )
                Column(
                    Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp
                    )
                    Text(
                        text = url,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = colorResource(id = jp.toastkid.lib.R.color.link_blue)
                    )
                    Text(
                        text = time,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = colorResource(id = jp.toastkid.lib.R.color.darkgray_scale)
                    )
                }
            }
        },
        modifier = modifier.padding(
            start = 8.dp,
            end = 8.dp,
            top = 2.dp,
            bottom = 2.dp
        )
    )
}

@Composable
private fun dateFormat(timeInMillis: Long) = DateFormat.format(
    stringResource(jp.toastkid.lib.R.string.date_format),
    timeInMillis
).toString()
