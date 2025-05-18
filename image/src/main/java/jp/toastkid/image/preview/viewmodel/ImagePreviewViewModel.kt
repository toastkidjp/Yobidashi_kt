/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.viewmodel

import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.IntOffset
import jp.toastkid.image.Image
import kotlin.math.max

class ImagePreviewViewModel(initialPage: Int) {

    private val pagerState = PagerState(initialPage, 0f) { pageCount() }

    fun pagerState(): PagerState = pagerState

    private fun isCurrentPage(page: Int) = page == pagerState.currentPage

    private val images = mutableStateListOf<Image>()

    private fun pageCount() = images.size

    fun replaceImages(images: Collection<Image>) {
        this.images.clear()
        this.images.addAll(images)
    }

    fun getCurrentImage() = getImage(pagerState.currentPage)

    fun getImage(page: Int) =
        images.getOrElse(page) { Image.makeEmpty() }

    private val scale = mutableFloatStateOf(1f)

    fun scale(page: Int) = if (isCurrentPage(page)) scale.value else 1f

    private val rotationY = mutableFloatStateOf(0f)

    fun rotationY(page: Int) = if (isCurrentPage(page)) rotationY.value else 0f

    fun flip() {
        rotationY.value = if (rotationY.value == 0f) 180f else 0f
    }

    private val rotationZ = mutableStateOf(0f)

    fun rotationZ(page: Int) = if (isCurrentPage(page)) rotationZ.value else 0f

    private val offset = mutableStateOf(Offset.Zero)

    fun offset(page: Int): IntOffset {
        if (page != pagerState.currentPage) {
            return@offset IntOffset.Zero
        }

        return IntOffset(
            offset.value.x.toInt(),
            offset.value.y.toInt()
        )
    }

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

    val openOtherMenu = mutableStateOf(false)

    val openDialog = mutableStateOf(false)

    fun closeDialog() {
        openDialog.value = false
    }

    private val transformable = mutableStateOf(false)

    fun transformable() = transformable.value

    fun setTransformable() {
        transformable.value = true
    }

    fun unsetTransformable() {
        transformable.value = false
        resetStates()
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
            if (colorFilterState.value != null) {
                null
            } else {
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
    }

    fun resetStates() {
        scale.floatValue = 1f
        offset.value = Offset.Zero
        rotationY.floatValue = 0f
        rotationZ.value = 0f
        transformable.value = false
    }

    fun sharedElementKey(page: Int): String {
        return "image_${
            getImage(page).path + if (pagerState.currentPage == page) "" else "_"
        }"
    }

}