/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.usecase

import android.content.Context
import android.util.LruCache
import jp.toastkid.api.suggestion.SuggestionApi
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchRepository
import jp.toastkid.yobidashi.search.history.SearchHistoryRepository
import jp.toastkid.yobidashi.search.url_suggestion.QueryUseCase
import jp.toastkid.yobidashi.search.viewmodel.SearchUiViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class QueryingUseCase(
    private val searchUiViewModel: SearchUiViewModel,
    private val preferenceApplier: PreferenceApplier,
    private val queryUseCase: QueryUseCase,
    private val favoriteSearchRepository: FavoriteSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val suggestionApi: SuggestionApi = SuggestionApi(),
    private val channel: Channel<String> = Channel(),
    private val cache: LruCache<String, List<String>> = LruCache<String, List<String>>(30),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private var disposables = Job()

    operator fun invoke(keyword: String) {
        if (preferenceApplier.isEnableSearchHistory) {
            CoroutineScope(Dispatchers.IO).launch {
                searchUiViewModel.searchHistories.clear()
                searchUiViewModel.searchHistories.addAll(
                    if (keyword.isNullOrBlank()) {
                        searchHistoryRepository.find(5)
                    } else {
                        searchHistoryRepository.select(keyword)
                    }
                )
            }
        }

        if (preferenceApplier.isEnableFavoriteSearch) {
            CoroutineScope(backgroundDispatcher).launch {
                searchUiViewModel.favoriteSearchItems.clear()
                searchUiViewModel.favoriteSearchItems.addAll(
                    if (keyword.isNullOrBlank()) {
                        favoriteSearchRepository.find(5)
                    } else {
                        favoriteSearchRepository.select(keyword)
                    }
                )
            }
        }

        if (preferenceApplier.isEnableViewHistory) {
            queryUseCase.invoke(keyword)
        }

        if (preferenceApplier.isEnableSuggestion) {
            querySuggestions(keyword)
        }
    }

    private fun querySuggestions(keyword: String) {
        if (cache.snapshot().containsKey(keyword)) {
            searchUiViewModel.suggestions.clear()
            searchUiViewModel.suggestions.addAll(cache.get(keyword))
            return
        }

        suggestionApi.fetchAsync(keyword) { suggestions ->
            searchUiViewModel.suggestions.clear()
            if (suggestions.isEmpty()) {
                return@fetchAsync
            }
            cache.put(keyword, suggestions)
            searchUiViewModel.suggestions.addAll(suggestions)
        }
    }

    fun send(key: String) {
        CoroutineScope(backgroundDispatcher).launch(disposables) { channel.send(key) }
    }

    fun withDebounce() {
        CoroutineScope(backgroundDispatcher).launch(disposables) {
            channel.receiveAsFlow()
                .distinctUntilChanged()
                .debounce(400)
                .collect {
                    invoke(it)
                }
        }
    }

    fun dispose() {
        disposables.cancel()
        channel.close()
    }

    private fun cannotUseNetwork(context: Context) =
        NetworkChecker.isNotAvailable(context) ||
                (PreferenceApplier(context).wifiOnly && NetworkChecker.isUnavailableWiFi(context))

}