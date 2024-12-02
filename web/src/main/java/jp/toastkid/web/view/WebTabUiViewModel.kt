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
import androidx.lifecycle.AtomicReference
import jp.toastkid.lib.ContentViewModel

/**
 * @author toastkidjp
 */
class WebTabUiViewModel {

    fun stopProgress() {
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

    private val _longTapActionParameters = mutableStateOf(EMPTY_LONG_TAP_ACTION_PARAMETERS)

    val longTapActionParameters: State<Triple<String?, String?, String?>> = _longTapActionParameters

    fun setLongTapParameters(title: String?, anchor: String?, imageUrl: String?) {
        _longTapActionParameters.value = Triple(title, anchor, imageUrl)
    }

    fun isOpenLongTapDialog() = longTapActionParameters.value !== EMPTY_LONG_TAP_ACTION_PARAMETERS

    fun clearLongTapParameters() {
        _longTapActionParameters.value = EMPTY_LONG_TAP_ACTION_PARAMETERS
    }

    private val readerModeText = AtomicReference<String>("")

    fun showReader(
        content: String,
        contentViewModel: ContentViewModel
    ) {
        if (isOpenReaderMode()) {
            closeReaderMode()
            return
        }

        val cleaned = content.replace("^\"|\"$".toRegex(), "")
        if (cleaned.isBlank()) {
            contentViewModel.snackShort("This page can't show reader mode.")
            return
        }

        val lineSeparator = System.lineSeparator()
        readerModeText.set(cleaned.replace("\\n", lineSeparator))
        openReaderMode.value = true
    }

    fun readerModeText(): String = readerModeText.get()

    private val openReaderMode = mutableStateOf(false)

    fun isOpenReaderMode() = openReaderMode.value

    fun closeReaderMode() {
        readerModeText.set("")
        openReaderMode.value = false
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

private val EMPTY_LONG_TAP_ACTION_PARAMETERS = Triple<String?, String?, String?>(null, null, null)
