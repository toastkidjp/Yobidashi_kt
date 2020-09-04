/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.graphics.Color
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.toastkid.lib.preference.ColorPair

/**
 * @author toastkidjp
 */
class MenuSwitchColorApplier(private val fab: FloatingActionButton) {

    private var previousIconColor: Int = Color.TRANSPARENT

    operator fun invoke(colorPair: ColorPair) {
        val newColor = colorPair.bgColor()
        if (previousIconColor != newColor) {
            previousIconColor = newColor
            colorPair.applyReverseTo(fab)
        }
    }
}