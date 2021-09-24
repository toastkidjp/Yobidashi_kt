/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.usecase

import androidx.annotation.VisibleForTesting
import jp.toastkid.lib.view.TextViewHighlighter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ContentTextSearchUseCase(
    private val textViewHighlighter: TextViewHighlighter,
    @VisibleForTesting private val inputChannel: Channel<String> = Channel(),
    @VisibleForTesting private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    @VisibleForTesting private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    operator fun invoke(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            return
        }

        CoroutineScope(backgroundDispatcher).launch {
            inputChannel.send(keyword)
        }
    }

    fun startObserve() {
        CoroutineScope(backgroundDispatcher).launch {
            inputChannel
                .receiveAsFlow()
                .distinctUntilChanged()
                .debounce(1000)
                .flowOn(mainDispatcher)
                .collect { textViewHighlighter(it) }
        }
    }

}