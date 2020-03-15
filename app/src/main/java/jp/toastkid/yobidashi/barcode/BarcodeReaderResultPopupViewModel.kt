/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.barcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class BarcodeReaderResultPopupViewModel : ViewModel() {

    private val _clip = MutableLiveData<String>()
    val clip: LiveData<String> = _clip

    fun clipText(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        _clip.postValue(text)
    }

    private val _share = MutableLiveData<String>()
    val share: LiveData<String> = _share

    fun shareText(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        _share.postValue(text)
    }

    private val _open = MutableLiveData<String>()
    val open: LiveData<String> = _open

    fun openText(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        _open.postValue(text)
    }

}