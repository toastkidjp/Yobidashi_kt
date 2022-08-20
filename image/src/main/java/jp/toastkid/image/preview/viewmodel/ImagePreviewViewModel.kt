/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.viewmodel

import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import jp.toastkid.image.Image
import kotlin.math.max

class ImagePreviewViewModel {

    private val images = mutableStateListOf<Image>()

    fun replaceImages(images: Collection<Image>) {
        this.images.clear()
        this.images.addAll(images)
    }

    fun getCurrentImage() =
        if (images.isNotEmpty()) images.get(index.value) else Image.makeEmpty()

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

    val openOtherMenu = mutableStateOf(false)

    val openDialog = mutableStateOf(false)

    fun moveToPrevious() {
        if (index.value == 0) {
            return
        }
        index.value--
    }

    fun moveToNext() {
        if (index.value >= images.size) {
            return
        }
        index.value++
    }

    fun setIndex(i: Int) {
        index.value = i
    }

    fun updateColorFilter() {
        val v = max(contrastSliderPosition.value, 0f) + 1f * (if (reverse.value) -1 else 1)
        val o = -128 * (v - 1)
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                v, 0f, 0f, alphaSliderPosition.value, o,
                0f, v, 0f, alphaSliderPosition.value, o,
                0f, 0f, v, alphaSliderPosition.value, o,
                0f, 0f, 0f, 1f, 000f
            )
        )
        if (saturation.value) {
            colorMatrix.setToSaturation(0.0f)
        }
        colorFilterState.value = ColorFilter.colorMatrix(colorMatrix)
    }

    fun setSepia() {
        colorFilterState.value =
            ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        0.9f, 0f, 0f, 0f, 000f,
                        0f, 0.7f, 0f, 0f, 000f,
                        0f, 0f, 0.4f, 0f, 000f,
                        0f, 0f, 0f, 1f, 000f
                    )
                )
            )
    }

    fun resetStates() {
        scale.value = 1f
        offset.value = Offset.Zero
        rotationY.value = 0f
        rotationZ.value = 0f
    }

}