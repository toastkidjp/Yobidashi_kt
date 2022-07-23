/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.view.Window
import androidx.core.graphics.ColorUtils
import jp.toastkid.lib.preference.ColorPair

/**
 * @author toastkidjp
 */
class WindowOptionColorApplier {

    operator fun invoke(window: Window, colorPair: ColorPair) {
        val color = ColorUtils.setAlphaComponent(colorPair.bgColor(), 255)
        window.statusBarColor     = color
        window.navigationBarColor = color
    }

}