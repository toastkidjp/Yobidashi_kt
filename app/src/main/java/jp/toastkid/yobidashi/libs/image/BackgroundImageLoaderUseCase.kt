/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.image

import android.widget.ImageView
import coil.load
import java.io.File

/**
 * @author toastkidjp
 */
class BackgroundImageLoaderUseCase {

    private var lastPath: String? = null

    operator fun invoke(target: ImageView, backgroundImagePath: String?) {
        if (backgroundImagePath.isNullOrEmpty() || backgroundImagePath.equals(lastPath)) {
            return
        }

        lastPath = backgroundImagePath

        target.load(File(backgroundImagePath)) {
            size(target.measuredWidth, target.measuredHeight)
        }
    }
}