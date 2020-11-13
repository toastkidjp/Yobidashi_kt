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
import jp.toastkid.lib.lifecycle.Event

/**
 * TODO introduce LiveData.
 * @author toastkidjp
 */
class PageSearcherViewModel : ViewModel() {

    val find = MutableLiveData<Event<String>>()

    val upward = MutableLiveData<Event<String>>()

    val downward = MutableLiveData<Event<String>>()

    val clear = MutableLiveData<Event<Unit>>()

    val close = MutableLiveData<Event<Unit>>()

    fun find(s: String?) {
        find.postValue(Event(s ?: ""))
    }

    fun findDown(s: String?) {
        downward.postValue(Event(s ?: ""))
    }

    fun findUp(s: String?) {
        upward.postValue(Event(s ?: ""))
    }

    fun findDown() {
        findDown(null)
    }

    fun findUp() {
        findUp(null)
    }

    fun hide() {
        close.postValue(Event(Unit))
    }

    fun clearInput() {
        clear.postValue(Event(Unit))
    }
}