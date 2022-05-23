/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import android.net.Uri
import android.os.Message
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class BrowserViewModel : ViewModel() {

    private val _preview =  MutableLiveData<Event<Uri>>()

    val preview: LiveData<Event<Uri>> = _preview

    fun preview(uri: Uri) {
        _preview.postValue(Event(uri))
    }

    private val _open = MutableLiveData<Event<Uri>>()

    val open: LiveData<Event<Uri>> = _open

    fun open(uri: Uri) {
        _open.postValue(Event(uri))
    }

    private val _openBackground = MutableLiveData<Event<Uri>>()

    val openBackground: LiveData<Event<Uri>> = _openBackground

    fun openBackground(uri: Uri) {
        _openBackground.postValue(Event(uri))
    }

    // TODO: Use appropriate data class.
    private val _openBackgroundWithTitle = MutableLiveData<Event<Pair<String, Uri>>>()

    val openBackgroundWithTitle: LiveData<Event<Pair<String, Uri>>> = _openBackgroundWithTitle

    fun openBackground(title: String, uri: Uri) {
        _openBackgroundWithTitle.postValue(Event(title to uri))
    }

    private val _openNewWindow = MutableLiveData<Event<Message?>>()

    val openNewWindow: LiveData<Event<Message?>> = _openNewWindow

    fun openNewWindow(resultMessage: Message?) {
        _openNewWindow.postValue(Event(resultMessage))
    }

    private val _download = MutableLiveData<Event<String>>()

    val download: LiveData<Event<String>> = _download

    fun download(url: String) {
        _download.postValue(Event(url))
    }

    private val _error = mutableStateOf("")
    val openErrorDialog = mutableStateOf(false)

    val error: State<String> = _error

    fun setError(text: String) {
        _error.value = text
        openErrorDialog.value = true
    }

    fun clearError() {
        _error.value = ""
        openErrorDialog.value = false
    }

    private val _longTapActionParameters =
        mutableStateOf(Triple<String?, String?, String?>(null, null, null))
    val openLongTapDialog = mutableStateOf(false)

    val longTapActionParameters: State<Triple<String?, String?, String?>> = _longTapActionParameters

    fun setLongTapParameters(title: String?, anchor: String?, imageUrl: String?) {
        _longTapActionParameters.value = Triple(title, anchor, imageUrl)
        openLongTapDialog.value = true
    }

    fun clearLongTapParameters() {
        _longTapActionParameters.value = Triple(null, null, null)
        openLongTapDialog.value = false
    }

    private val _switchWebViewToCurrent = MutableLiveData<Event<String>>()

    val switchWebViewToCurrent: LiveData<Event<String>> = _switchWebViewToCurrent

    fun switchWebViewToCurrent(tabId: String) {
        _switchWebViewToCurrent.postValue(Event(tabId))
    }

    //TODO WIP

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    fun nextTitle(nextTitle: String?) {
        if (nextTitle.isNullOrBlank()) {
            return
        }
        _title.value = nextTitle ?: ""
    }

    private val _url = mutableStateOf("")
    val url: State<String> = _url

    fun nextUrl(nextUrl: String?) {
        if (nextUrl.isNullOrBlank()) {
            return
        }
        _url.value = nextUrl ?: ""
    }

    private val _enableBackPress = mutableStateOf(false)

    val enableBackPress: State<Boolean> = _enableBackPress

    fun setEnableBackPress(newState: Boolean) {
        _enableBackPress.value = newState
    }

    private val _enableForward = mutableStateOf(false)

    val enableForward: State<Boolean> = _enableForward

    fun setForwardButtonIsEnabled(newState: Boolean) {
        _enableForward.value = newState
    }

    private val _enableBack = mutableStateOf(false)

    val enableBack: State<Boolean> = _enableBack

    fun setBackButtonIsEnabled(newState: Boolean) {
        _enableBack.value = newState
        setEnableBackPress(newState)
    }

    private val _progress = mutableStateOf(0)

    val progress: State<Int> = _progress

    fun updateProgress(newProgress: Int) {
        _progress.value = newProgress
    }

    private val _stopProgress = mutableStateOf(Event(false))

    val stopProgress: State<Event<Boolean>> = _stopProgress

    fun stopProgress(stop: Boolean) {
        _stopProgress.value = Event(stop)
    }
}