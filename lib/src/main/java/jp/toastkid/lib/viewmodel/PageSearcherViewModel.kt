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
import androidx.lifecycle.viewModelScope
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.lib.viewmodel.event.finder.ClearFinderInputEvent
import jp.toastkid.lib.viewmodel.event.finder.CloseFinderEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class PageSearcherViewModel : ViewModel() {

    private val _event = MutableSharedFlow<jp.toastkid.lib.viewmodel.event.Event>()

    val event = _event.asSharedFlow()

    private val _find = MutableLiveData<Event<String>>()

    private val _upward = MutableLiveData<Event<String>>()

    private val _downward = MutableLiveData<Event<String>>()

    private val _clear = MutableLiveData<Event<Unit>>()

    private val _close = MutableLiveData<Event<Unit>>()

    val find: LiveData<Event<String>> = _find

    val upward: LiveData<Event<String>> = _upward

    val downward: LiveData<Event<String>> = _downward

    val clear: LiveData<Event<Unit>> = _clear

    val close: LiveData<Event<Unit>> = _close

    fun find(s: String?) {
        viewModelScope.launch {
            _event.emit(FindInPageEvent(s ?: ""))
        }
    }

    fun findDown(s: String?) {
        viewModelScope.launch {
            _event.emit(FindInPageEvent(s ?: ""))
        }
    }

    fun findUp(s: String?) {
        viewModelScope.launch {
            _event.emit(FindInPageEvent(s ?: "", true))
        }
    }

    fun hide() {
        viewModelScope.launch {
            _event.emit(CloseFinderEvent())
        }
    }

    fun clearInput() {
        viewModelScope.launch {
            _event.emit(ClearFinderInputEvent())
        }
    }
}