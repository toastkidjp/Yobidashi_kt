/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.graphics.Bitmap

/**
 * @author toastkidjp
 */
class ImageRotationUseCase(
        private val viewModel: ImagePreviewFragmentViewModel,
        private val currentBitmap: () -> Bitmap?
) {
    private val rotatedBitmapFactory = RotatedBitmapFactory()

    fun rotateLeft() {
        currentBitmap()?.let {
            viewModel.nextBitmap(rotatedBitmapFactory.rotateLeft(it))
        }
    }

    fun rotateRight() {
        currentBitmap()?.let {
            viewModel.nextBitmap(rotatedBitmapFactory.rotateRight(it))
        }
    }

    fun reverse() {
        currentBitmap()?.let {
            viewModel.nextBitmap(rotatedBitmapFactory.reverse(it))
        }
    }
}