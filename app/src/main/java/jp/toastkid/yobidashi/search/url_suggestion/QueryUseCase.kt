/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.url_suggestion

import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
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
    private val switchVisibility: (Boolean) -> Unit
) {

    operator fun invoke(q: CharSequence) {
        adapter.clear()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (q.isBlank()) {
                    return@withContext
                }
                bookmarkRepository.search("%$q%", ITEM_LIMIT).forEach { adapter.add(it) }
            }

            withContext(Dispatchers.IO) {
                viewHistoryRepository.search("%$q%", ITEM_LIMIT).forEach { adapter.add(it) }
            }

            switchVisibility(adapter.isNotEmpty())
            adapter.notifyDataSetChanged()
        }
    }

    companion object {

        /**
         * Item limit.
         */
        private const val ITEM_LIMIT = 3

    }

}