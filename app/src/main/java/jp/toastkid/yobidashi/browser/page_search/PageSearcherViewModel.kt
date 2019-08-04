/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.page_search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class PageSearcherViewModel : ViewModel() {

    val upward = MutableLiveData<String?>()

    val downward = MutableLiveData<String?>()

    val clear = MutableLiveData<Unit>()

    val close = MutableLiveData<Unit>()

    fun findDown(s: String?) {
        downward.postValue(s)
    }

    fun findUp(s: String?) {
        upward.postValue(s)
    }

    fun findDown() {
        findDown(null)
    }

    fun findUp() {
        findUp(null)
    }

    fun hide() {
        close.postValue(Unit)
    }

    fun clearInput() {
        clear.postValue(Unit)
    }
}