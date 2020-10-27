/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.io.File
import kotlin.math.max

/**
 * @author toastkidjp
 */
class ViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {

    fun setImage(path: String) {
        view.load(File(path)) { size(max(view.width, view.height)) }
    }

}