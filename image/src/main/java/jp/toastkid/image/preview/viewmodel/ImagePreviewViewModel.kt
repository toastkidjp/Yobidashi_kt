/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.viewmodel

import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.ViewModel

class ImagePreviewViewModel : ViewModel() {

    var scale = mutableStateOf(1f)

    var rotationY = mutableStateOf(0f)

    var rotationZ = mutableStateOf(0f)

    var offset = mutableStateOf(Offset.Zero)

    val state = TransformableState { zoomChange, offsetChange, rotationChange ->
        scale.value *= zoomChange
        rotationZ.value += rotationChange
        offset.value += offsetChange
    }

    var alphaSliderPosition = mutableStateOf(0f)

    var contrastSliderPosition = mutableStateOf(0f)

    val saturation = mutableStateOf(false)

    val reverse = mutableStateOf(false)

    val openMenu = mutableStateOf(false)

    val colorFilterState = mutableStateOf<ColorFilter?>(null)

    val index = mutableStateOf(0)

    fun setIndex(i: Int) {
        index.value = i
    }

}