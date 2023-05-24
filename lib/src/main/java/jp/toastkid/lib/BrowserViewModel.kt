/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import jp.toastkid.lib.view.swiperefresh.SwipeRefreshState

/**
 * @author toastkidjp
 */
class BrowserViewModel {

    fun openBackground() {
        /*viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri, true))
        }*/
    }

    fun openBackground(title: String, uri: Uri) {
        /*viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri, true, title))
        }*/
    }

    fun openNewWindow(resultMessage: Message?) {
        /*viewModelScope.launch {
            _event.emit(OpenNewWindowEvent(resultMessage))
        }*/
    }

    fun download(url: String) {
        /*viewModelScope.launch {
            _event.emit(DownloadEvent(url))
        }*/
    }

    suspend fun stopProgress(stop: Boolean) {
        swipeRefreshState.value?.resetOffset()
        swipeRefreshState.value?.isRefreshing = false
    }

    fun search(query: String) {
        /*viewModelScope.launch {
            _event.emit(WebSearchEvent(query))
        }*/
    }

    private val _icon = mutableStateOf<Bitmap?>(null)

    val icon: State<Bitmap?> = _icon

    fun newIcon(bitmap: Bitmap) {
        _icon.value = bitmap
    }

    fun resetIcon() {
        _icon.value = null
    }

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    fun nextTitle(nextTitle: String?) {
        if (nextTitle.isNullOrBlank()) {
            return
        }
        _title.value = nextTitle
    }

    private val _url = mutableStateOf("")
    val url: State<String> = _url

    fun nextUrl(nextUrl: String?) {
        if (nextUrl.isNullOrBlank()) {
            return
        }
        _url.value = nextUrl
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
    }

    private val _progress = mutableStateOf(100)

    val progress: State<Int> = _progress

    fun updateProgress(newProgress: Int) {
        _progress.value = newProgress
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

    val swipeRefreshState = mutableStateOf<SwipeRefreshState?>(null)

    fun initializeSwipeRefreshState(refreshTriggerPx: Float) {
        swipeRefreshState.value = SwipeRefreshState(false, refreshTriggerPx)
    }

    fun showSwipeRefreshIndicator() =
        swipeRefreshState.value?.isSwipeInProgress == true
                || swipeRefreshState.value?.isRefreshing == true

    fun calculateSwipeRefreshIndicatorAlpha(refreshTriggerPx: Float) =
        ((swipeRefreshState.value?.indicatorOffset ?: 0f) / refreshTriggerPx).coerceIn(0f, 1f)

    fun calculateSwipingProgress(refreshTriggerPx: Float) =
        if (swipeRefreshState.value?.isRefreshing == false)
                ((swipeRefreshState.value?.indicatorOffset ?: 0f) / refreshTriggerPx)
                    .coerceIn(0f, 1f)
        else
            progress.value.toFloat() / 100f

    val openLongTapDialog = mutableStateOf(false)

    private val _longTapActionParameters =
        mutableStateOf(Triple<String?, String?, String?>(null, null, null))

    val longTapActionParameters: State<Triple<String?, String?, String?>> = _longTapActionParameters

    fun setLongTapParameters(title: String?, anchor: String?, imageUrl: String?) {
        _longTapActionParameters.value = Triple(title, anchor, imageUrl)
    }

    fun clearLongTapParameters() {
        _longTapActionParameters.value = Triple(null, null, null)
    }

}