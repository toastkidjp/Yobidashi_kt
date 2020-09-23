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
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class BarcodeReaderResultPopupViewModel : ViewModel() {

    private val _clip = MutableLiveData<Event<String>>()
    val clip: LiveData<Event<String>> = _clip

    fun clipText(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        _clip.postValue(Event(text))
    }

    private val _share = MutableLiveData<Event<String>>()
    val share: LiveData<Event<String>> = _share

    fun shareText(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        _share.postValue(Event(text))
    }

    private val _open = MutableLiveData<Event<String>>()
    val open: LiveData<Event<String>> = _open

    fun openText(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        _open.postValue(Event(text))
    }

}