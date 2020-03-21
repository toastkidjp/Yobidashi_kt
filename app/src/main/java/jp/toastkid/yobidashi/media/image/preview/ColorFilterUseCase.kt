/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter

/**
 * @author toastkidjp
 */
class ColorFilterUseCase(private val viewModel: ImagePreviewFragmentViewModel) {

    private var lastFilter: ImageColorFilter? = null

    fun applyAlpha(alpha: Float) {
        lastFilter = null
        viewModel.newColorFilter(
                ColorMatrixColorFilter(
                        ColorMatrix().also {
                            it.set(
                                    floatArrayOf(
                                            1f,0f,0f,alpha,000f,
                                            0f,1f,0f,alpha,000f,
                                            0f,0f,1f,alpha,000f,
                                            0f,0f,0f,1f,000f
                                    )
                            )
                        }
                )
        )
    }

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
    }
}