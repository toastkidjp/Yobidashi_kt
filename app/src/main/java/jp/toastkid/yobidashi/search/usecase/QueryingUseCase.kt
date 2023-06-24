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
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchRepository
import jp.toastkid.yobidashi.search.history.SearchHistoryRepository
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
import kotlinx.coroutines.withContext

class QueryingUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val viewHistoryRepository: ViewHistoryRepository,
    private val favoriteSearchRepository: FavoriteSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val suggestionApi: SuggestionApi = SuggestionApi(),
    private val channel: Channel<String> = Channel(),
    private val cache: LruCache<String, List<String>> = LruCache<String, List<String>>(30),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private var disposables = Job()

    private fun invoke(keyword: String) {
        val viewModel = viewModel ?: return

        if (viewModel.isEnableSearchHistory()) {
            CoroutineScope(Dispatchers.IO).launch(disposables) {
                viewModel.searchHistories.clear()
                viewModel.searchHistories.addAll(
                    if (keyword.isBlank()) {
                        searchHistoryRepository.find(5)
                    } else {
                        searchHistoryRepository.select(keyword)
                    }
                )
            }
        }

        if (viewModel.isEnableFavoriteSearch()) {
            CoroutineScope(backgroundDispatcher).launch(disposables) {
                viewModel.favoriteSearchItems.clear()
                viewModel.favoriteSearchItems.addAll(
                    if (keyword.isBlank()) {
                        favoriteSearchRepository.find(5)
                    } else {
                        favoriteSearchRepository.select(keyword)
                    }
                )
            }
        }

        if (viewModel.isEnableViewHistory()) {
            CoroutineScope(backgroundDispatcher).launch {
                val newItems = mutableListOf<UrlItem>()

                withContext(ioDispatcher) {
                    if (keyword.isBlank()) {
                        return@withContext
                    }
                    bookmarkRepository.search("%$keyword%", ITEM_LIMIT).forEach { newItems.add(it) }
                }

                withContext(ioDispatcher) {
                    viewHistoryRepository.search("%$keyword%", ITEM_LIMIT).forEach { newItems.add(it) }
                }

                viewModel.urlItems.clear()
                viewModel.urlItems.addAll(newItems)
            }
        }

        if (viewModel.isEnableSuggestion()) {
            if (cache.snapshot().containsKey(keyword)) {
                viewModel.suggestions.clear()
                viewModel.suggestions.addAll(cache.get(keyword))
                return
            }

            suggestionApi.fetchAsync(keyword) { suggestions ->
                viewModel.suggestions.clear()
                if (suggestions.isEmpty()) {
                    return@fetchAsync
                }
                cache.put(keyword, suggestions)
                viewModel.suggestions.addAll(suggestions)
            }
        }
    }

    fun send(key: String) {
        CoroutineScope(backgroundDispatcher).launch(disposables) { channel.send(key) }
    }

    fun withDebounce() {
        CoroutineScope(backgroundDispatcher).launch(disposables) {
            channel.receiveAsFlow()
                .distinctUntilChanged()
                .debounce(100)
                .collect {
                    invoke(it)
                }
        }
    }

    fun dispose() {
        disposables.cancel()
        channel.close()
    }

    private var viewModel: SearchUiViewModel? = null

    fun setViewModel(searchUiViewModel: SearchUiViewModel) {
        viewModel = searchUiViewModel
    }

    companion object {
        /**
         * Item limit.
         */
        private const val ITEM_LIMIT = 3

        fun make(context: Context): QueryingUseCase {
            val repositoryFactory = RepositoryFactory()
            return QueryingUseCase(
                repositoryFactory.bookmarkRepository(context),
                repositoryFactory.viewHistoryRepository(context),
                repositoryFactory.favoriteSearchRepository(context),
                repositoryFactory.searchHistoryRepository(context)
            )
        }

    }

}