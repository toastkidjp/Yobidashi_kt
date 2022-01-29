/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class PageSearcherViewModel : ViewModel() {

    private val _find = MutableLiveData<String>()

    private val _upward = MutableLiveData<String>()

    private val _downward = MutableLiveData<String>()

    private val _clear = MutableLiveData<Event<Unit>>()

    private val _close = MutableLiveData<Event<Unit>>()

    val find: LiveData<String> = _find

    val upward: LiveData<String> = _upward

    val downward: LiveData<String> = _downward

    val clear: LiveData<Event<Unit>> = _clear

    val close: LiveData<Event<Unit>> = _close

    fun find(s: String?) {
        _find.postValue(s ?: "")
    }

    fun findDown(s: String?) {
        _downward.postValue(s ?: "")
    }

    fun findUp(s: String?) {
        _upward.postValue(s ?: "")
    }

    fun findDown() {
        findDown(null)
    }

    fun findUp() {
        findUp(null)
    }

    fun hide() {
        _close.postValue(Event(Unit))
    }

    fun clearInput() {
        _clear.postValue(Event(Unit))
    }
}