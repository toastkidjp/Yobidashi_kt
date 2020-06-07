/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.image

import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import timber.log.Timber
import java.io.File

/**
 * @author toastkidjp
 */
class BackgroundImageLoaderUseCase {

    private var lastPath: String? = null

    operator fun invoke(target: ImageView, backgroundImagePath: String?) {
        if (backgroundImagePath.isNullOrEmpty() || backgroundImagePath.equals(lastPath)) {
            Timber.i("tomato skip")
            return
        }

        lastPath = backgroundImagePath

        Glide.with(target)
                .load(File(backgroundImagePath).toURI().toString().toUri())
                .override(target.measuredWidth, target.measuredHeight)
                .into(target)
    }
}