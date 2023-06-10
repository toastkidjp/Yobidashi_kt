/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import jp.toastkid.lib.viewmodel.event.web.WebSearchEvent
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import jp.toastkid.yobidashi.search.history.SearchHistory
import jp.toastkid.api.trend.Trend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SearchUiViewModel(private val workDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    private val _input = mutableStateOf(TextFieldValue())

    val input: State<TextFieldValue> = _input

    fun setInput(textInputValue: TextFieldValue) {
        _input.value = textInputValue
    }

    fun putQuery(query: String) {
        _input.value = TextFieldValue(query, TextRange(query.length), TextRange.Zero)
    }

    val urlItems = mutableStateListOf<UrlItem>()

    val trends = mutableStateListOf<Trend>()

    val suggestions = mutableStateListOf<String>()

    val favoriteSearchItems = mutableStateListOf<FavoriteSearch>()

    val searchHistories = mutableStateListOf<SearchHistory>()

    private val _search = MutableSharedFlow<WebSearchEvent>()

    val search = _search.asSharedFlow()

    fun search(query: String) {
        CoroutineScope(workDispatcher).launch {
            _search.emit(WebSearchEvent(query))
        }
    }

    fun searchWithCategory(query: String, category: String, background: Boolean = false) {
        CoroutineScope(Dispatchers.Default).launch {
            _search.emit(WebSearchEvent(query, category, background))
        }
    }

    fun searchOnBackground(query: String) {
        CoroutineScope(workDispatcher).launch {
            _search.emit(WebSearchEvent(query, background = true))
        }
    }

    fun enableBackHandler() =
        openSearchHistory.value || openFavoriteSearch.value

    fun closeOption() {
        _openSearchHistory.value = false
        _openFavoriteSearch.value = false
    }

    private val _openSearchHistory = mutableStateOf(false)

    val openSearchHistory: State<Boolean> = _openSearchHistory

    fun openSearchHistory() {
        _openSearchHistory.value = true
    }

    private val _openFavoriteSearch = mutableStateOf(false)

    val openFavoriteSearch: State<Boolean> = _openFavoriteSearch

    fun openFavoriteSearch() {
        _openFavoriteSearch.value = true
    }

}