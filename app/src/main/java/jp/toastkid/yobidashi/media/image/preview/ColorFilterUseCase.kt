/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

/**
 * @author toastkidjp
 */
class ColorFilterUseCase(private val viewModel: ImagePreviewFragmentViewModel) {

    private var lastFilter: ImageColorFilter? = null

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