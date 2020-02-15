/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class BrowserViewModel : ViewModel() {

    private val _preview =  MutableLiveData<Uri>()

    val preview: LiveData<Uri> = _preview

    fun preview(uri: Uri) {
        _preview.postValue(uri)
    }

    private val _open = MutableLiveData<Uri>()

    val open: LiveData<Uri> = _open

    fun open(uri: Uri) {
        _open.postValue(uri)
    }

    private val _openBackground = MutableLiveData<Uri>()

    val openBackground: LiveData<Uri> = _openBackground

    fun openBackground(uri: Uri) {
        _openBackground.postValue(uri)
    }
}