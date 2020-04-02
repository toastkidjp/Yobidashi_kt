/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class ImageViewerFragmentViewModel : ViewModel() {

    private val _onClick = MutableLiveData<String>()

    val onClick: LiveData<String> = _onClick

    fun click(name: String) {
        _onClick.postValue(name)
    }

    private val _onLongClick = MutableLiveData<String>()

    val onLongClick: LiveData<String> = _onLongClick

    fun longClick(name: String) {
        _onLongClick.postValue(name)
    }
}