/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.history

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
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
    val searchHistoryItems = remember { mutableStateListOf<SearchHistory>() }

    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    val openConfirmDialog = remember { mutableStateOf(false) }

    Surface(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .background(colorResource(id = R.color.setting_background))
                .nestedScroll(rememberViewInteropNestedScrollConnection())
        ) {
            coroutineScope.launch {
                val loaded = withContext(Dispatchers.IO) {
                    searchHistoryRepository.findAll()
                }
                searchHistoryItems.clear()
                searchHistoryItems.addAll(loaded)
            }

            items(searchHistoryItems) { searchHistory ->
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
    }

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
}
