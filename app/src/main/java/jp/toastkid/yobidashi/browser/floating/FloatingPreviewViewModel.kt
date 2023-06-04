/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * @author toastkidjp
 */
class FloatingPreviewViewModel {

    private val _title = mutableStateOf("")

    val title: State<String> = _title

    fun newTitle(title: String?) {
        _title.value = title ?: ""
    }

    private val _icon = mutableStateOf<Bitmap?>(null)

    val icon: State<Bitmap?> = _icon

    fun newIcon(bitmap: Bitmap?) {
        _icon.value = bitmap
    }

    private val _url = mutableStateOf("")

    val url: State<String> = _url

    fun newUrl(url: String?) {
        _url.value = url ?: ""
    }

    private val _progress = mutableStateOf(0)

    val progress: State<Int> = _progress

    fun newProgress(progress: Int) {
        _progress.value = progress
    }
}