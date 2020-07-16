/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab.tab_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

/**
 * @author toastkidjp
 */
internal class TabListViewModel : ViewModel() {

    private val _saveEditorTab = MutableLiveData<File>()

    val saveEditorTab: LiveData<File> = _saveEditorTab

    fun saveEditorTab(nextFile: File) {
        _saveEditorTab.postValue(nextFile)
    }

    private val _tabCount = MutableLiveData<Int>()

    val tabCount: LiveData<Int> = _tabCount

    fun tabCount(count: Int) {
        _tabCount.postValue(count)
    }

}