/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.background

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import coil.load
import jp.toastkid.yobidashi.R

class ImageLoadingUseCase {

    operator fun invoke(contentView: View, arguments: Bundle) {
        val imageView = contentView.findViewById<ImageView>(R.id.image)
        when {
            arguments.containsKey(KEY_IMAGE) -> {
                val bitmap = arguments.getParcelable<Bitmap>(KEY_IMAGE)
                imageView.load(bitmap)
            }
            arguments.containsKey(KEY_IMAGE_URL) -> {
                val uriString = arguments.getString(KEY_IMAGE_URL)
                imageView.load(uriString)
            }
        }
    }

    companion object {

        private const val KEY_IMAGE = "image"

        private const val KEY_IMAGE_URL = "imageUrl"

    }

}