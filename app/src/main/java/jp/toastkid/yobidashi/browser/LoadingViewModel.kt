/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser

import androidx.lifecycle.ViewModel
import jp.toastkid.yobidashi.tab.History
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * @author toastkidjp
 */
class LoadingViewModel : ViewModel() {

    private val _onPageFinished = MutableSharedFlow<Pair<String, History>>()

    val onPageFinished: SharedFlow<Pair<String, History>> = _onPageFinished

    suspend fun finished(tabId: String, history: History) = _onPageFinished.emit(tabId to history)
}