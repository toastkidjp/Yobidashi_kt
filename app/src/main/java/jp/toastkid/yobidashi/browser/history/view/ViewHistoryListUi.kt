/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.history.view

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.list.ListActionAttachment
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.history.ViewHistory
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.view.BindItemContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ViewHistoryListUi() {
    val context = LocalContext.current as? ComponentActivity ?: return

    val database = DatabaseFinder().invoke(context)
    val viewHistoryRepository = database.viewHistoryRepository()
    val fullItems = mutableListOf<ViewHistory>()
    val viewHistoryItems = remember { mutableStateListOf<ViewHistory>() }
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = "first_launch", block = {
        CoroutineScope(Dispatchers.Main).launch {
            val loaded = withContext(Dispatchers.IO) {
                viewHistoryRepository.reversed()
            }
            fullItems.clear()
            fullItems.addAll(loaded)
            viewHistoryItems.addAll(fullItems)
        }
    })

    val browserViewModel = ViewModelProvider(context).get(BrowserViewModel::class.java)

    val onClick: (ViewHistory, Boolean) -> Unit = { viewHistory, isLongClick ->
        when (isLongClick) {
            true -> {
                browserViewModel.openBackground(viewHistory.title, Uri.parse(viewHistory.url))
            }
            false -> {
                browserViewModel.open(Uri.parse(viewHistory.url))
            }
        }
    }

    List(viewHistoryItems, listState, onClick, {
        viewHistoryRepository.delete(it)
        viewHistoryItems.remove(it)
    })

    val clearConfirmDialogState = remember { mutableStateOf(false) }
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    contentViewModel.optionMenus(
        OptionMenu(titleId = R.string.title_clear_view_history, action = {
            clearConfirmDialogState.value = true
        })
    )

    ListActionAttachment.make(context)
        .invoke(
            listState,
            LocalLifecycleOwner.current,
            viewHistoryItems,
            fullItems,
            { item, word -> item.title.contains(word) || item.url.contains(word) }
        )

    DestructiveChangeConfirmDialog(
        clearConfirmDialogState,
        R.string.title_clear_view_history
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                viewHistoryRepository.deleteAll()
            }
            viewHistoryItems.clear()

            contentViewModel.snackShort(R.string.done_clear)
        }
    }
}

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
                }
            )
        }
    }

}