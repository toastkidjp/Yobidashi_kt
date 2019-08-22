/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.view

import android.os.Build
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * @author toastkidjp
 */
class ToolbarColorApplier {

    operator fun invoke(window: Window, toolbar: Toolbar, colorPair: ColorPair) {
        toolbar.let {
            it.setBackgroundColor(colorPair.bgColor())

            val fontColor = colorPair.fontColor()
            it.setTitleTextColor(fontColor)
            it.setSubtitleTextColor(fontColor)

            Colors.applyTint(it.navigationIcon, fontColor)
            Colors.applyTint(it.overflowIcon, fontColor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color = ColorUtils.setAlphaComponent(colorPair.bgColor(), 255)
            window.statusBarColor     = color
            window.navigationBarColor = color
        }
    }
}