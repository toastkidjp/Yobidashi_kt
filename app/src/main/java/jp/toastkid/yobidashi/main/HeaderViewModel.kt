/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class HeaderViewModel : ViewModel() {

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    fun updateProgress(newProgress: Int) {
        _progress.postValue(newProgress)
    }

    private val _stopProgress = MutableLiveData<Boolean>()
    val stopProgress: LiveData<Boolean> = _stopProgress

    fun stopProgress(stop: Boolean) {
        _stopProgress.postValue(stop)
    }

    private val _content = MutableLiveData<View>()

    val content: LiveData<View> = _content

    fun replace(view: View) {
        _content.postValue(view)
    }

}