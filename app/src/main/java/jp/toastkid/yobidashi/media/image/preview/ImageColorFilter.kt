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
enum class ImageColorFilter(val filter: ColorMatrixColorFilter) {
    SEPIA(
            ColorMatrixColorFilter(
                    ColorMatrix().also {
                        it.set(
                                floatArrayOf(
                                        0.9f,0f,0f,0f,000f,
                                        0f,0.7f,0f,0f,000f,
                                        0f,0f,0.4f,0f,000f,
                                        0f,0f,0f,1f,000f
                                )
                        )
                    }
            )
    ),
    REVERSE(
            ColorMatrixColorFilter(
                    ColorMatrix().also {
                        it.set(
                                floatArrayOf(
                                        -1f,0f,0f,0f,255f,
                                        0f,-1f,0f,0f,255f,
                                        0f,0f,-1f,0f,255f,
                                        0f,0f,0f,1f,255f
                                )
                        )
                    }
            )
    ),

    GRAY_SCALE(ColorMatrixColorFilter(ColorMatrix().also{ it.setSaturation(0.0f) }));

}