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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.lib.viewmodel.event.tab.OpenNewTabEvent
import jp.toastkid.lib.viewmodel.event.tab.SaveEditorTabEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author toastkidjp
 */
class TabListViewModel : ViewModel() {

    private val _event = MutableSharedFlow<jp.toastkid.lib.viewmodel.event.Event>()

    val event = _event.asSharedFlow()

    private val _saveEditorTab = MutableLiveData<File>()

    val saveEditorTab: LiveData<File> = _saveEditorTab

    fun saveEditorTab(nextFile: File) {
        viewModelScope.launch {
            _event.emit(SaveEditorTabEvent(nextFile))
        }
    }

    private val _tabCount = mutableStateOf(0)

    val tabCount: State<Int> = _tabCount

    fun tabCount(count: Int) {
        _tabCount.value = count
    }

    private val _openNewTab = MutableLiveData<Event<Unit>>()

    val openNewTab: LiveData<Event<Unit>> = _openNewTab

    fun openNewTab() {
        viewModelScope.launch {
            _event.emit(OpenNewTabEvent())
        }
    }

}