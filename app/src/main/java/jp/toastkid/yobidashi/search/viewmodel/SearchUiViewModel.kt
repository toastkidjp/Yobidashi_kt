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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.search.value.SearchEvent
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import jp.toastkid.yobidashi.search.history.SearchHistory
import jp.toastkid.yobidashi.search.trend.Trend

class SearchUiViewModel : ViewModel() {

    private val _input = mutableStateOf(TextFieldValue())

    val input: State<TextFieldValue> = _input

    fun setInput(textInputValue: TextFieldValue) {
        _input.value = textInputValue
    }

    val urlItems = mutableStateListOf<UrlItem>()

    val trends = mutableStateListOf<Trend>()

    val suggestions = mutableStateListOf<String>()

    val favoriteSearchItems = mutableStateListOf<FavoriteSearch>()

    val searchHistories = mutableStateListOf<SearchHistory>()

    private val _search = MutableLiveData<Event<SearchEvent>>()

    val search: LiveData<Event<SearchEvent>> = _search

    fun search(query: String) {
        _search.postValue(Event(SearchEvent(query)))
    }

    fun searchWithCategory(query: String, category: String, background: Boolean = false) {
        _search.postValue(Event(SearchEvent(query, category, background)))
    }

    fun searchOnBackground(query: String) {
        _search.postValue(Event(SearchEvent(query, background = true)))
    }

    private val _putQuery = MutableLiveData<Event<String>>()

    val putQuery: LiveData<Event<String>> = _putQuery

    fun putQuery(query: String) {
        _putQuery.postValue(Event(query))
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