/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class BrowserHeaderViewModel : ViewModel() {

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?> = _title

    fun nextTitle(nextTitle: String?) {
        _title.postValue(nextTitle)
    }

    private val _url = MutableLiveData<String?>()
    val url: LiveData<String?> = _url

    fun nextUrl(nextUrl: String?) {
        _url.postValue(nextUrl)
    }

    private val _reset = MutableLiveData<Unit>()
    val reset: LiveData<Unit> = _reset

    fun resetContent() {
        _reset.postValue(Unit)
    }

    private val _enableForward = MutableLiveData<Boolean>()
    val enableForward: LiveData<Boolean> = _enableForward
    fun setForwardButtonEnability(newState: Boolean) {
        _enableForward.postValue(newState)
    }

    private val _enableBack = MutableLiveData<Boolean>()
    val enableBack: LiveData<Boolean> = _enableBack
    fun setBackButtonEnability(newState: Boolean) {
        _enableBack.postValue(newState)
    }

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

}