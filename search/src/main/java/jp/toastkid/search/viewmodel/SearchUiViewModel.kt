/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import jp.toastkid.api.trend.Trend
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.web.WebSearchEvent
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.usecase.QueryingUseCase
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import jp.toastkid.yobidashi.search.history.SearchHistory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SearchUiViewModel(
    private val queryingUseCase: QueryingUseCase,
    private val workDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    init {
        queryingUseCase.setViewModel(this)
    }

    private val _input = mutableStateOf(TextFieldValue())

    val input: State<TextFieldValue> = _input

    fun setInput(textInputValue: TextFieldValue) {
        _input.value = textInputValue
        queryingUseCase.send(textInputValue.text)
    }

    fun putQuery(query: String) {
        _input.value = TextFieldValue(query, TextRange(query.length), TextRange.Zero)
    }

    val urlItems = mutableStateListOf<UrlItem>()

    private val trends = mutableStateListOf<Trend>()

    fun trends() = trends

    fun replaceTrends(newTrends: List<Trend>?) {
        trends.clear()
        newTrends?.let {
            trends.addAll(it.take(10))
        }
    }

    private val suggestions = mutableStateListOf<String>()

    fun suggestions() = suggestions

    fun replaceSuggestions(newItems: List<String>) {
        suggestions.clear()
        suggestions.addAll(newItems.take(10))
    }

    fun clearSuggestions() {
        suggestions.clear()
    }

    fun showSuggestions() = suggestions.isNotEmpty()

    private val favoriteSearchItems = mutableStateListOf<FavoriteSearch>()

    fun enableFavoriteSearchItems(): Boolean {
        return favoriteSearchItems.isNotEmpty()
    }

    fun favoriteSearchItems() = favoriteSearchItems

    fun removeFavoriteSearchItems(favoriteSearch: FavoriteSearch) {
        favoriteSearchItems.remove(favoriteSearch)
    }

    fun replaceFavoriteSearch(newList: List<FavoriteSearch>) {
        favoriteSearchItems.clear()
        favoriteSearchItems.addAll(newList)
    }

    private val searchHistories = mutableStateListOf<SearchHistory>()

    fun enableShowSearchHistories() = searchHistories.isNotEmpty()

    fun searchHistories() = searchHistories

    fun replaceSearchHistory(newHistories: List<SearchHistory>) {
        searchHistories.clear()
        searchHistories.addAll(newHistories)
    }

    fun removeSearchHistories(target: SearchHistory) {
        searchHistories.remove(target)
    }

    fun clearSearchHistories() {
        searchHistories.clear()
    }

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

    fun startReceiver() {
        queryingUseCase.withDebounce()
    }

    fun dispose() {
        queryingUseCase.dispose()
    }

    private val isEnableSearchHistory = mutableStateOf(false)
    private val isEnableFavoriteSearch = mutableStateOf(false)
    private val isEnableViewHistory = mutableStateOf(false)
    private val isEnableSuggestion = mutableStateOf(false)
    private val isEnableTrend = mutableStateOf(false)
    private val isEnableUrlCard = mutableStateOf(false)

    fun isEnableSearchHistory() = isEnableSearchHistory.value

    fun isEnableFavoriteSearch() = isEnableFavoriteSearch.value

    fun isEnableSuggestion() = isEnableSuggestion.value

    fun isEnableViewHistory() = isEnableViewHistory.value

    fun useTrend() = isEnableTrend.value && trends.isNotEmpty()

    fun useUrlCard() = isEnableUrlCard.value

    fun copyFrom(preferenceApplier: PreferenceApplier) {
        if (isEnableSearchHistory.value != preferenceApplier.isEnableSearchHistory) {
            isEnableSearchHistory.value = preferenceApplier.isEnableSearchHistory
        }
        if (isEnableFavoriteSearch.value != preferenceApplier.isEnableFavoriteSearch) {
            isEnableFavoriteSearch.value = preferenceApplier.isEnableFavoriteSearch
        }
        if (isEnableViewHistory.value != preferenceApplier.isEnableViewHistory) {
            isEnableViewHistory.value = preferenceApplier.isEnableViewHistory
        }
        if (isEnableSuggestion.value != preferenceApplier.isEnableSuggestion) {
            isEnableSuggestion.value = preferenceApplier.isEnableSuggestion
        }
        if (isEnableTrend.value != preferenceApplier.isEnableTrendModule()) {
            isEnableTrend.value = preferenceApplier.isEnableTrendModule()
        }
        if (isEnableUrlCard.value != preferenceApplier.isEnableUrlModule()) {
            isEnableUrlCard.value = preferenceApplier.isEnableUrlModule()
        }
    }

    private val categoryName = mutableStateOf(SearchCategory.getDefaultCategoryName())

    fun setCategory(newCategoryName: SearchCategory) {
        categoryName.value = newCategoryName.name
    }

    fun setCategoryName(newCategoryName: String) {
        categoryName.value = newCategoryName
    }

    fun categoryName() = categoryName.value

}