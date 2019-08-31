/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class MenuViewModel : ViewModel() {
    val click = MutableLiveData<Menu>()
    val longClick = MutableLiveData<Menu>()
    val visibility = MutableLiveData<Boolean>()
    val tabCount = MutableLiveData<Int>()
    val onResume = MutableLiveData<Unit>()

    fun tabCount(count: Int) {
        this.tabCount.postValue(count)
    }

    fun onResume() {
        onResume.postValue(Unit)
    }

    fun close() {
        visibility.postValue(false)
    }
}