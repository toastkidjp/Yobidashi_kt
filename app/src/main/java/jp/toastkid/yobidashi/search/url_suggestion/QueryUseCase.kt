/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.url_suggestion

import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class QueryUseCase(
    private val adapter: Adapter,
    private val bookmarkRepository: BookmarkRepository,
    private val viewHistoryRepository: ViewHistoryRepository,
    private val switchVisibility: (Boolean) -> Unit,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(q: CharSequence) {
        CoroutineScope(mainDispatcher).launch {
            val newItems = mutableListOf<UrlItem>()

            RtsSuggestionUseCase({ newItems.add(0, it) }).invoke(q.toString())

            withContext(ioDispatcher) {
                if (q.isBlank()) {
                    return@withContext
                }
                bookmarkRepository.search("%$q%", ITEM_LIMIT).forEach { newItems.add(it) }
            }

            withContext(ioDispatcher) {
                viewHistoryRepository.search("%$q%", ITEM_LIMIT).forEach { newItems.add(it) }
            }

            switchVisibility(newItems.isNotEmpty())
            adapter.submitList(newItems)
        }
    }

    companion object {

        /**
         * Item limit.
         */
        private const val ITEM_LIMIT = 3

    }

}