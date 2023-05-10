/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.viewmodel.event.Event
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * @author toastkidjp
 */
class TabListViewModel : ViewModel() {

    private val _event = MutableSharedFlow<Event>()

    val event = _event.asSharedFlow()

    private val _tabCount = mutableStateOf(0)

    val tabCount: State<Int> = _tabCount

    fun tabCount(count: Int) {
        _tabCount.value = count
    }

}