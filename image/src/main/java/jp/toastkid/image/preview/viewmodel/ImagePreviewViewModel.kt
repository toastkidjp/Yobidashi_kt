/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.viewmodel

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import jp.toastkid.image.Image
import jp.toastkid.image.preview.state.PreviewImageState
import kotlin.math.max

class ImagePreviewViewModel(initialPage: Int) {

    private val pagerState = PagerState(initialPage, 0f, ::pageCount)

    fun pagerState(): PagerState = pagerState

    private val images = mutableStateListOf<Image>()

    private fun pageCount() = images.size

    fun replaceImages(images: Collection<Image>) {
        this.images.clear()
        this.images.addAll(images)
    }

    fun getCurrentImage() = getImage(pagerState.currentPage)

    fun getImage(page: Int) =
        images.getOrElse(page) { Image.makeEmpty() }

    private val previewImageStateHolder = mutableMapOf<Int, PreviewImageState>()

    private fun getPreviewImageState(page: Int): PreviewImageState {
        val imageState = previewImageStateHolder.get(page)
        if (imageState != null) {
            return imageState
        }

        val newState = PreviewImageState()
        previewImageStateHolder.put(page, newState)
        return newState
    }

    fun scale(page: Int) = getPreviewImageState(page).scale()

    fun currentScale() = getPreviewImageState(pagerState.currentPage).scale()

    suspend fun clearPreviousState() {
        val page = pagerState.currentPage
        if (previewImageStateHolder.size > 3) {
            val keepIndices = arrayOf(page - 1, page, page + 1)
            val iterator = previewImageStateHolder.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val index = entry.key
                if (keepIndices.contains(index).not()) {
                    iterator.remove()
                    continue
                }
                if (index != page) {
                    entry.value.reset()
                }
            }
        }
    }

    fun rotationY(page: Int) = getPreviewImageState(page).rotationY()

    fun flip() {
        getPreviewImageState(pagerState.currentPage).flip()
    }

    fun rotationZ(page: Int) = getPreviewImageState(page).rotationZ()

    fun offset(page: Int): IntOffset {
        val range = arrayOf(pagerState.currentPage -1, pagerState.currentPage, pagerState.currentPage + 1)
        if (range.contains(page).not()) {
            return IntOffset.Zero
        }

        val currentPainterSize = painterSize.value * currentScale() / 2f

        val offset = getPreviewImageState(page).offset()
        return IntOffset(
            offset.x.coerceIn(-currentPainterSize.width, currentPainterSize.width).toInt(),
            offset.y.coerceIn(-currentPainterSize.height, currentPainterSize.height).toInt()
        )
    }

    suspend fun onGesture(offsetChange: Offset, zoomChange: Float, rotationChange: Float) {
        val newOffsetChange = if (outOfRange(offsetChange)) Offset.Zero else offsetChange
        getPreviewImageState(pagerState.currentPage).onGesture(newOffsetChange, zoomChange, rotationChange)
    }

    val alphaSliderPosition = mutableFloatStateOf(0f)

    val contrastSliderPosition = mutableFloatStateOf(0f)

    val saturation = mutableStateOf(false)

    val reverse = mutableStateOf(false)

    val openMenu = mutableStateOf(false)

    val colorFilterState = mutableStateOf<ColorFilter?>(null)

    val openOtherMenu = mutableStateOf(false)

    fun closeOtherMenu() {
        openOtherMenu.value = false
    }

    val openDialog = mutableStateOf(false)

    fun closeDialog() {
        openDialog.value = false
    }

    suspend fun rotateLeft() {
        getPreviewImageState(pagerState.currentPage).rotateLeft()
    }

    suspend fun rotateRight() {
        getPreviewImageState(pagerState.currentPage).rotateRight()
    }

    fun updateColorFilter() {
        val v = max(contrastSliderPosition.floatValue, 0f) + 1f * (if (reverse.value) -1 else 1)
        val o = -128 * (v - 1)
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                v, 0f, 0f, alphaSliderPosition.floatValue, o,
                0f, v, 0f, alphaSliderPosition.floatValue, o,
                0f, 0f, v, alphaSliderPosition.floatValue, o,
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

    fun sharedElementKey(page: Int): String {
        return "image_${
            getImage(page).path + if (pagerState.currentPage == page) "" else "_"
        }"
    }

    private val currentSize = mutableStateOf(Size.Zero)

    fun setCurrentSize(it: IntSize) {
        currentSize.value = it.toSize()
    }

    fun currentSize(): Size {
        return currentSize.value
    }

    private val painterSize = mutableStateOf(Size.Zero)

    fun setPainterSize(intrinsicSize: Size) {
        this.painterSize.value = intrinsicSize
    }

    suspend fun zoom(newOffset: Offset) {
        getPreviewImageState(pagerState.currentPage)
            .zoom(currentSize(), newOffset)
    }

    fun outOfRange(panChange: Offset): Boolean {
        val currentScale = currentScale()
        val range = painterSize.value / currentScale
        val rangeLeft = (range.width)
        val rangeRight = rangeLeft * -1
        val x = getPreviewImageState(pagerState.currentPage).offset().x
        val y = getPreviewImageState(pagerState.currentPage).offset().y
        if (panChange.x < 0 && x < 0 && rangeRight > x) {
            return true
        }
        if (panChange.x > 0 && x > 0 && rangeLeft < x) {
            return true
        }

        return false
    }

    fun snapPositionalThreshold() = snapPositionalThreshold

    suspend fun resetPagerScrollState() {
        pagerState.scrollBy(pagerState.currentPageOffsetFraction)
    }

    fun resetOffset() {
        getPreviewImageState(pagerState.currentPage).resetOffset()
    }

}

private const val snapPositionalThreshold = 0.2f