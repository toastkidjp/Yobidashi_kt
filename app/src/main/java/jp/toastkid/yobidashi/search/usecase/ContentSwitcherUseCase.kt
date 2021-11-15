/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.usecase

import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @param channel Use for handling input action
 */
class ContentSwitcherUseCase(
    private val binding: FragmentSearchBinding?,
    private val preferenceApplier: PreferenceApplier,
    private val setActionButtonState: (Boolean) -> Unit,
    private val currentTitle: String?,
    private val currentUrl: String?,
    private val channel: Channel<String> = Channel(),
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private var disposables = Job()

    operator fun invoke(key: String) {
        if (key.isEmpty() || key == currentUrl) {
            binding?.urlCard?.switch(currentTitle, currentUrl)
        } else {
            binding?.urlCard?.hide()
        }

        setActionButtonState(key.isEmpty())

        if (preferenceApplier.isEnableSearchHistory) {
            binding?.searchHistoryCard?.query(key)
        }

        binding?.favoriteSearchCard?.query(key)
        binding?.urlSuggestionCard?.query(key)

        if (preferenceApplier.isDisableSuggestion) {
            binding?.suggestionCard?.clear()
            return
        }

        binding?.suggestionCard?.request(key)
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
                    withContext(mainDispatcher) { invoke(it) }
                }
        }
    }

    fun dispose() {
        disposables.cancel()
        channel.close()
    }

}