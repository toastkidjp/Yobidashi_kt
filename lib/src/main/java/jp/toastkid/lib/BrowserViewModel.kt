/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import android.net.Uri
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
}