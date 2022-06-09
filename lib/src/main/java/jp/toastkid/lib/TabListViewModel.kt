/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import java.io.File

/**
 * @author toastkidjp
 */
class TabListViewModel : ViewModel() {

    private val _saveEditorTab = MutableLiveData<File>()

    val saveEditorTab: LiveData<File> = _saveEditorTab

    fun saveEditorTab(nextFile: File) {
        _saveEditorTab.postValue(nextFile)
    }

    private val _tabCount = mutableStateOf<Int>(0)

    val tabCount: MutableState<Int> = _tabCount

    fun tabCount(count: Int) {
        _tabCount.value = count
    }

    private val _openNewTab = MutableLiveData<Event<Unit>>()

    val openNewTab: LiveData<Event<Unit>> = _openNewTab

    fun openNewTab() {
        _openNewTab.postValue(Event(Unit))
    }

    fun openNewTabForLongTap(): Boolean {
        _openNewTab.postValue(Event(Unit))
        return true
    }

}