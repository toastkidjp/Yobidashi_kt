/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.os.Build
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import java.io.File

/**
 * @author toastkidjp
 */
class ViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {

    fun setImage(path: String) {
        view.load(File(path), imageLoader)
    }

    private val imageLoader = ImageLoader.Builder(view.context)
            .componentRegistry {
                add(if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder() else GifDecoder())
            }
            .build()

}