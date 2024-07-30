/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.floating

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf

/**
 * @author toastkidjp
 */
class FloatingPreviewViewModel {

    private val _title = mutableStateOf("")

    fun title() = _title.value

    fun newTitle(title: String?) {
        _title.value = title ?: ""
    }

    private val _icon = mutableStateOf<Bitmap?>(null)

    fun icon() = _icon.value

    fun newIcon(bitmap: Bitmap?) {
        _icon.value = bitmap
    }

    private val _url = mutableStateOf("")

    fun url() = _url.value

    fun newUrl(url: String?) {
        _url.value = url ?: ""
    }

    private val _progress = mutableStateOf(0)

    fun progress() = _progress.value

    fun newProgress(progress: Int) {
        _progress.value = progress
    }
}