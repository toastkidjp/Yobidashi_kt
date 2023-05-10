/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.toastkid.lib.viewmodel.event.Event
import jp.toastkid.lib.viewmodel.event.finder.CloseFinderEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class PageSearcherViewModel : ViewModel() {

    private val _event = MutableSharedFlow<Event>()

    val event = _event.asSharedFlow()

    fun find(s: String?) {
        viewModelScope.launch {
            _event.emit(FindInPageEvent(s ?: ""))
        }
    }

    fun hide() {
        viewModelScope.launch {
            _event.emit(CloseFinderEvent())
        }
    }

}