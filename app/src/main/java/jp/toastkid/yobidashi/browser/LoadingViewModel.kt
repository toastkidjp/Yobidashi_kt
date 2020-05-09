/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.yobidashi.tab.History

/**
 * @author toastkidjp
 */
class LoadingViewModel : ViewModel() {

    private val _onPageFinished = MutableLiveData<Pair<String, History>>()

    val onPageFinished: LiveData<Pair<String, History>> = _onPageFinished

    fun finished(tabId: String, history: History) = _onPageFinished.postValue(tabId to history)
}