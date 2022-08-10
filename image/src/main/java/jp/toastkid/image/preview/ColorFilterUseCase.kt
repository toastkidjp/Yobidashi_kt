/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.preview

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import kotlin.math.max

/**
 * @author toastkidjp
 */
class ColorFilterUseCase(private val viewModel: ImagePreviewFragmentViewModel) {

    private var lastFilter: ImageColorFilter? = null

    fun applyAlpha(alpha: Float) {
        lastFilter = null
        viewModel.newColorFilter(
            makeColorMatrixColorFilter(
                floatArrayOf(
                    1f, 0f, 0f, alpha, 000f,
                    0f, 1f, 0f, alpha, 000f,
                    0f, 0f, 1f, alpha, 000f,
                    0f, 0f, 0f, 1f, 000f
                )
            )
        )
    }

    fun applyContrast(contrast: Float) {
        lastFilter = null
        val v = max(contrast, 0f) + 1f
        val o = -128 * (v - 1)
        viewModel.newColorFilter(
            makeColorMatrixColorFilter(
                floatArrayOf(
                    v, 0f, 0f, 0f, o,
                    0f, v, 0f, 0f, o,
                    0f, 0f, v, 0f, o,
                    0f, 0f, 0f, 1f, 000f
                )
            )
        )
    }

    private fun makeColorMatrixColorFilter(floats: FloatArray) =
        ColorMatrixColorFilter(ColorMatrix().also { it.set(floats) })

    fun reverseFilter() {
        applyFilter(ImageColorFilter.REVERSE)
    }

    fun sepia() {
        applyFilter(ImageColorFilter.SEPIA)
    }

    fun grayScale() {
        applyFilter(ImageColorFilter.GRAY_SCALE)
    }

    private fun applyFilter(imageColorFilter: ImageColorFilter) {
        if (lastFilter == imageColorFilter) {
            clearFilter()
            return
        }
        lastFilter = imageColorFilter
        viewModel.newColorFilter(imageColorFilter)
    }

    private fun clearFilter() {
        viewModel.clearFilter()
        lastFilter = null
    }
}