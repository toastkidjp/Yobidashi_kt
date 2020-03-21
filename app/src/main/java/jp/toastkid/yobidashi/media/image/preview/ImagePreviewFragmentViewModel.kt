/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.graphics.ColorFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class ImagePreviewFragmentViewModel : ViewModel() {

    private val _colorFilter = MutableLiveData<ColorFilter?>()

    val colorFilter: LiveData<ColorFilter?> = _colorFilter

    fun newColorFilter(filter: ImageColorFilter) {
        _colorFilter.postValue(filter.filter)
    }

    fun clearFilter() {
        _colorFilter.postValue(null)
    }
}