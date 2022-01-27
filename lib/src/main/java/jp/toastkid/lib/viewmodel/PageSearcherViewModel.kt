/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event

/**
 * TODO introduce LiveData.
 * @author toastkidjp
 */
class PageSearcherViewModel : ViewModel() {

    val find = MutableLiveData<String>()

    val upward = MutableLiveData<String>()

    val downward = MutableLiveData<String>()

    val clear = MutableLiveData<Event<Unit>>()

    val close = MutableLiveData<Event<Unit>>()

    fun find(s: String?) {
        find.postValue(s ?: "")
    }

    fun findDown(s: String?) {
        downward.postValue(s ?: "")
    }

    fun findUp(s: String?) {
        upward.postValue(s ?: "")
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