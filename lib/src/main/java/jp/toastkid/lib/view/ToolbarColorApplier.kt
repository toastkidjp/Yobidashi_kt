/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.content.res.ColorStateList
import android.os.Build
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomappbar.BottomAppBar
import jp.toastkid.lib.preference.ColorPair

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

            it.navigationIcon?.let { DrawableCompat.setTint(it, fontColor) }
            it.overflowIcon?.let { DrawableCompat.setTint(it, fontColor) }
        }

        if (toolbar is BottomAppBar) {
            toolbar.backgroundTint = ColorStateList.valueOf(colorPair.bgColor())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color = ColorUtils.setAlphaComponent(colorPair.bgColor(), 255)
            window.statusBarColor     = color
            window.navigationBarColor = color
        }
    }
}