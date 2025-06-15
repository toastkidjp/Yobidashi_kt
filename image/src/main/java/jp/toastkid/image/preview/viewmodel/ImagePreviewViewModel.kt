/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.viewmodel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.TransformableState
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
import androidx.lifecycle.AtomicReference
import jp.toastkid.image.Image
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    private val scale = mutableMapOf<Int, Animatable<Float, AnimationVector1D>>()

    fun scale(page: Int) = if (isCurrentPage(page)) scale.getOrElse(page, { Animatable(1f) }).value else 1f

    fun currentScale() = scale.getOrElse(pagerState.currentPage, { Animatable(1f) }).value

    fun clearPreviousState() {
        if (pagerState.currentPage != pagerState.settledPage) {
            scale.remove(pagerState.settledPage)
        }
    }

    private val rotationY = mutableFloatStateOf(0f)

    fun rotationY(page: Int) = if (isCurrentPage(page)) rotationY.floatValue else 0f

    fun flip() {
        rotationY.value = if (rotationY.value == 0f) 180f else 0f
    }

    private val rotationZ = mutableFloatStateOf(0f)

    fun rotationZ(page: Int) = if (isCurrentPage(page)) rotationZ.value else 0f

    private val offset = mutableStateOf(Offset.Zero)

    fun offset(page: Int): IntOffset {
        if (page != pagerState.currentPage) {
            return@offset IntOffset.Zero
        }

        val currentPainterSize = painterSize.value * currentScale() / 2f
        return IntOffset(
            offset.value.x.coerceIn(-currentPainterSize.width, currentPainterSize.width).toInt(),
            offset.value.y.coerceIn(-currentPainterSize.height, currentPainterSize.height).toInt()
        )
    }

    // TODO Delete it
    val state = TransformableState { zoomChange, offsetChange, rotationChange ->
        rotationZ.value += rotationChange
        //scale.snapTo(scale.value * zoomChange)
        offset.value += offsetChange
    }

    suspend fun onGesture(offsetChange: Offset, zoomChange: Float, rotationChange: Float) {
        rotationZ.value += rotationChange
        val scale = this.scale.getOrElse(pagerState.currentPage, { Animatable(1f) })
        scale.snapTo(scale.value * zoomChange)
        val absX = abs(offsetChange.x)
        val absY = abs(offsetChange.y)
        offset.value += when {
            scale.value != 1f -> offsetChange
            absX > absY -> Offset(offsetChange.x, 0f)
            absY > absX -> Offset(0f, offsetChange.y)
            else -> Offset(offsetChange.x, 0f)
        }
    }

    val alphaSliderPosition = mutableStateOf(0f)

    val contrastSliderPosition = mutableStateOf(0f)

    val saturation = mutableStateOf(false)

    val reverse = mutableStateOf(false)

    val openMenu = mutableStateOf(false)

    val colorFilterState = mutableStateOf<ColorFilter?>(null)

    val openOtherMenu = mutableStateOf(false)

    val openDialog = mutableStateOf(false)

    fun closeDialog() {
        openDialog.value = false
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

    suspend fun resetStates() {
        /*if (currentScale() != 1f) {
            scale.snapTo(1f)
        }*/
        scale.put(pagerState.currentPage, Animatable(1f))
        offset.value = Offset.Zero
        rotationY.floatValue = 0f
        rotationZ.value = 0f
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
        val unset = currentScale() != 1f

        val newScale = if (unset) 1f else 3f

        if (unset) {
            rotationY.floatValue = 0f
            rotationZ.value = 0f
            this.offset.value = Offset.Zero
            scale.get(pagerState.currentPage)?.animateTo(1f)
            return
        }

        val newLayoutRect = this.currentSize.value / 2f

        this.offset.value = Offset(
            (-1 * (newOffset.x - newLayoutRect.width)).coerceIn(-newLayoutRect.width, newLayoutRect.width),
            (-1 * (newOffset.y - newLayoutRect.height)).coerceIn(-newLayoutRect.height, newLayoutRect.height)
        )
        scale.get(pagerState.currentPage)?.animateTo(newScale)
    }

    fun outOfRange(panChange: Offset): Boolean {
        val currentScale = currentScale()
        val range = painterSize.value / currentScale
        val rangeLeft = (range.width / (currentScale + 1f))
        val rangeRight = rangeLeft * -1
        if (panChange.x < 0 && offset.value.x < 0 && rangeRight > offset.value.x) {
            return true
        }
        if (panChange.x > 0 && offset.value.x > 0 && rangeLeft < offset.value.x) {
            return true
        }

        return false
    }

    suspend fun movePageWithFraction() {
        val currentPageOffsetFraction = pagerState.currentPageOffsetFraction
        if (currentScale() != 1f) {
            return
        }

        if (abs(currentPageOffsetFraction) <= snapPositionalThreshold) {
            pagerState.animateScrollToPage(pagerState.currentPage)
            return
        }

        val targetPage = if (currentPageOffsetFraction > 0) {
            min(
                pagerState.currentPage + 1,
                pagerState.pageCount - 1
            )
        } else {
            max(
                pagerState.currentPage - 1,
                0
            )
        }
        pagerState.animateScrollToPage(targetPage)
    }

    fun snapPositionalThreshold() = snapPositionalThreshold

    suspend fun resetPagerScrollState() {
        pagerState.scrollBy(pagerState.currentPageOffsetFraction)
    }

}

private const val snapPositionalThreshold = 0.2f