/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
data class Theme(
        @ColorInt val backgroundColor: Int,
        @ColorInt val fontColor: Int,
        @ColorInt val editorBackgroundColor: Int,
        @ColorInt val editorFontColor: Int,
        @ColorInt val editorCursorColor: Int,
        @ColorInt val editorHighlightColor: Int,
        val useDarkMode: Boolean
) {

    fun apply(preferenceApplier: PreferenceApplier) {
        preferenceApplier.color = backgroundColor
        preferenceApplier.fontColor = fontColor
        preferenceApplier.setEditorBackgroundColor(editorBackgroundColor)
        preferenceApplier.setEditorFontColor(editorFontColor)
        preferenceApplier.setEditorCursorColor(editorCursorColor)
        preferenceApplier.setEditorHighlightColor(editorHighlightColor)
        preferenceApplier.setUseDarkMode(useDarkMode)
    }

    companion object {
        fun extract(preferenceApplier: PreferenceApplier, substituteCursorColor: Int, substituteHilightColor: Int): Theme {
            return Theme(
                    preferenceApplier.color,
                    preferenceApplier.fontColor,
                    preferenceApplier.editorBackgroundColor(),
                    preferenceApplier.editorFontColor(),
                    preferenceApplier.editorCursorColor(substituteCursorColor),
                    preferenceApplier.editorHighlightColor(substituteHilightColor),
                    preferenceApplier.useDarkMode()
            )
        }
    }
}