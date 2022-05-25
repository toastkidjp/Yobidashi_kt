/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.history

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.list.ListActionAttachment
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.view.SearchItemContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SearchHistoryListUi() {
    val context = LocalContext.current

    val database = DatabaseFinder().invoke(LocalContext.current)
    val searchHistoryRepository = database.searchHistoryRepository()
    val fullItems = mutableListOf<SearchHistory>()
    val searchHistoryItems = remember { mutableStateListOf<SearchHistory>() }

    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    val openConfirmDialog = remember { mutableStateOf(false) }

    LazyColumn(state = listState) {
        items(searchHistoryItems, { it.key }) { searchHistory ->
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

    LaunchedEffect(key1 = "initial_load", block = {
        val loaded = withContext(Dispatchers.IO) {
            searchHistoryRepository.findAll()
        }
        searchHistoryItems.clear()
        searchHistoryItems.addAll(loaded)
        fullItems.addAll(loaded)
    })

    viewModel(modelClass = ContentViewModel::class.java).optionMenus(
        OptionMenu(
            titleId = R.string.title_clear_search_history,
            action = {
                openConfirmDialog.value = true
            }
        )
    )

    DestructiveChangeConfirmDialog(
        openConfirmDialog,
        titleId = R.string.title_clear_search_history
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                DatabaseFinder().invoke(context).searchHistoryRepository().deleteAll()
            }

            (context as? ComponentActivity)?.let { activity ->
                ViewModelProvider(activity).get(ContentViewModel::class.java)
                    .snackShort(R.string.settings_color_delete)
            }
        }
    }

    val viewLifecycleOwner = LocalLifecycleOwner.current
    val viewModelStoreOwner = context as? ViewModelStoreOwner
    if (viewModelStoreOwner != null) {
        ListActionAttachment.make(viewModelStoreOwner)
            .invoke(
                listState,
                viewLifecycleOwner,
                searchHistoryItems,
                fullItems,
                { item, word -> item.query?.contains(word) == true }
            )
    }
}
