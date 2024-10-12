/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.web.view.refresh.SwipeRefreshState

/**
 * @author toastkidjp
 */
class WebTabUiViewModel {

    suspend fun stopProgress() {
        swipeRefreshState.value?.resetOffset()
        swipeRefreshState.value?.isRefreshing = false
        resetRefreshing()
    }

    private val _icon = mutableStateOf<Bitmap?>(null)

    fun icon() = _icon.value

    fun newIcon(bitmap: Bitmap) {
        _icon.value = bitmap
    }

    fun resetIcon() {
        _icon.value = null
    }

    private val _title = mutableStateOf("")

    fun title() = _title.value

    fun nextTitle(nextTitle: String?) {
        if (nextTitle.isNullOrBlank()) {
            return
        }
        _title.value = nextTitle
    }

    private val _url = mutableStateOf("")

    fun url() = _url.value

    fun nextUrl(nextUrl: String?) {
        if (nextUrl.isNullOrBlank()) {
            return
        }
        _url.value = nextUrl
    }

    private val _enableForward = mutableStateOf(false)

    fun enableForward() = _enableForward.value

    fun setForwardButtonIsEnabled(newState: Boolean) {
        _enableForward.value = newState
    }

    private val _enableBack = mutableStateOf(false)

    fun enableBack() = _enableBack.value

    fun setBackButtonIsEnabled(newState: Boolean) {
        _enableBack.value = newState
    }

    private val _progress = mutableIntStateOf(100)

    fun progress() = _progress.intValue

    fun updateProgress(newProgress: Int) {
        _progress.intValue = newProgress

        if (newProgress > 70) {
            resetRefreshing()
        }
    }

    fun shouldShowProgressIndicator(): Boolean {
        return _progress.intValue < 70
    }

    private val _error = mutableStateOf("")

    private val openErrorDialog = mutableStateOf(false)

    fun openErrorDialog() = openErrorDialog.value

    fun closeErrorDialog() {
        openErrorDialog.value = false
    }

    fun error() = _error.value

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
            _progress.intValue.toFloat() / 100f

    private val openLongTapDialog = mutableStateOf(false)

    fun openLongTapDialog() {
        openLongTapDialog.value = true
    }

    fun isOpenLongTapDialog() = openLongTapDialog.value

    private val _longTapActionParameters =
        mutableStateOf(Triple<String?, String?, String?>(null, null, null))

    val longTapActionParameters: State<Triple<String?, String?, String?>> = _longTapActionParameters

    fun setLongTapParameters(title: String?, anchor: String?, imageUrl: String?) {
        _longTapActionParameters.value = Triple(title, anchor, imageUrl)
    }

    fun clearLongTapParameters() {
        _longTapActionParameters.value = Triple(null, null, null)
        openLongTapDialog.value = false
    }

    private val readerModeText = mutableStateOf("")

    fun showReader(
        content: String,
        contentViewModel: ContentViewModel
    ) {
        val cleaned = content.replace("^\"|\"$".toRegex(), "")
        if (cleaned.isBlank()) {
            contentViewModel.snackShort("This page can't show reader mode.")
            return
        }

        val lineSeparator = System.lineSeparator()
        readerModeText.value = cleaned.replace("\\n", lineSeparator)
    }

    fun readerModeText() = readerModeText.value

    fun isOpenReaderMode() =
        readerModeText.value.isNotBlank()

    fun closeReaderMode() {
        readerModeText.value = ""
    }

    private val refreshing = mutableStateOf(false)

    fun isRefreshing() = refreshing.value

    fun setRefreshing() {
        refreshing.value = true
    }

    private fun resetRefreshing() {
        refreshing.value = false
    }

}