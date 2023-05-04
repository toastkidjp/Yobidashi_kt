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
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.toastkid.lib.model.LoadInformation
import jp.toastkid.lib.view.swiperefresh.SwipeRefreshState
import jp.toastkid.lib.viewmodel.event.Event
import jp.toastkid.lib.viewmodel.event.web.DownloadEvent
import jp.toastkid.lib.viewmodel.event.web.OnLoadCompletedEvent
import jp.toastkid.lib.viewmodel.event.web.OnStopLoadEvent
import jp.toastkid.lib.viewmodel.event.web.OpenNewWindowEvent
import jp.toastkid.lib.viewmodel.event.web.OpenUrlEvent
import jp.toastkid.lib.viewmodel.event.web.PreviewUrlEvent
import jp.toastkid.lib.viewmodel.event.web.SwitchWebViewToCurrentEvent
import jp.toastkid.lib.viewmodel.event.web.WebSearchEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class BrowserViewModel : ViewModel() {

    private val _event = MutableSharedFlow<Event>()

    val event: SharedFlow<Event> = _event

    fun preview(uri: Uri) {
        viewModelScope.launch {
            _event.emit(PreviewUrlEvent(uri))
        }
    }

    fun open(uri: Uri) {
        viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri))
        }
    }

    fun openBackground(uri: Uri) {
        viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri, true))
        }
    }

    fun openBackground(title: String, uri: Uri) {
        viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri, true, title))
        }
    }

    fun openNewWindow(resultMessage: Message?) {
        viewModelScope.launch {
            _event.emit(OpenNewWindowEvent(resultMessage))
        }
    }

    fun download(url: String) {
        viewModelScope.launch {
            _event.emit(DownloadEvent(url))
        }
    }

    fun switchWebViewToCurrent(tabId: String) {
        viewModelScope.launch {
            _event.emit(SwitchWebViewToCurrentEvent(tabId))
        }
    }

    fun stopProgress(stop: Boolean) {
        viewModelScope.launch {
            _event.emit(OnStopLoadEvent())
        }
    }

    fun finished(tabId: String, title: String, url: String) =
        viewModelScope.launch {
            _event.emit(OnLoadCompletedEvent(LoadInformation(tabId, title, url)))
        }

    fun search(query: String) {
        viewModelScope.launch {
            _event.emit(WebSearchEvent(query))
        }
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

    private val _progress = mutableStateOf(0)

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

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    fun nestedScrollDispatcher() = nestedScrollDispatcher

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

}