/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.graphics.ColorFilter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.max

/**
 * @author toastkidjp
 */
class ViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {

    fun setImage(path: String) {
        Glide.with(view)
                .load(File(path))
                .override(max(view.width, view.height))
                .into(view)
    }

    fun setColorFilter(colorFilter: ColorFilter?) {
        view.colorFilter = colorFilter
    }

}