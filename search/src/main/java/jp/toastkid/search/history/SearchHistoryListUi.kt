/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.history

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.scroll.StateScrollerFactory
import jp.toastkid.search.R
import jp.toastkid.search.SearchAction
import jp.toastkid.search.view.SearchItemContent
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.search.history.SearchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SearchHistoryListUi() {
    val context = LocalContext.current

    val searchHistoryRepository = remember { RepositoryFactory().searchHistoryRepository(context) }
    val searchHistoryItems = remember { mutableStateListOf<SearchHistory>() }

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

    val contentViewModel = viewModel(modelClass = ContentViewModel::class.java)
    val fullItems = remember { mutableListOf<SearchHistory>() }

    LaunchedEffect(key1 = "initial_load", block = {
        val loaded = withContext(Dispatchers.IO) {
            searchHistoryRepository.findAll()
        }
        searchHistoryItems.clear()
        searchHistoryItems.addAll(loaded)
        fullItems.addAll(loaded)

        contentViewModel.optionMenus(
            OptionMenu(
                titleId = R.string.title_clear_search_history,
                action = {
                    openConfirmDialog.value = true
                }
            )
        )
    })

    if (openConfirmDialog.value) {
        DestructiveChangeConfirmDialog(
            titleId = R.string.title_clear_search_history,
            onDismissRequest = { openConfirmDialog.value = false }
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    searchHistoryRepository.deleteAll()
                }

                contentViewModel
                    .snackShort(R.string.settings_color_delete)
            }
        }
    }

    val viewLifecycleOwner = LocalLifecycleOwner.current
    val viewModelStoreOwner = context as? ViewModelStoreOwner
    if (viewModelStoreOwner != null) {
        LaunchedEffect(viewLifecycleOwner) {
            withContext(Dispatchers.IO) {
                ViewModelProvider(viewModelStoreOwner).get(ContentViewModel::class)
                    .receiveEvent(
                        StateScrollerFactory().invoke(listState),
                        searchHistoryItems,
                        fullItems.toList()
                    ) { item, word -> item.query?.contains(word) == true }
            }
        }
    }
}
