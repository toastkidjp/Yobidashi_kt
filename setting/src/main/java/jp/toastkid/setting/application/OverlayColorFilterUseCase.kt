/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.setting.application

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class OverlayColorFilterUseCase(
        private val preferenceApplier: PreferenceApplier,
        private val contentViewModel: ContentViewModel?
) {

    private val blueBase = Color(0xDD81D4FA).toArgb()

    private val redBase = Color(0xDDffcdd2).toArgb()

    private val yellowBase = Color(0x22777700).toArgb()

    private val redYellowBase = Color(0xFFFF9100).toArgb()

    private val orangeBase = Color(0xDDFF5722).toArgb()

    private val greenBase = Color(0xCCCDDC39).toArgb()

    private val darkBase = Color(0x66000000).toArgb()

    fun setBlue() {
        setNewColor(currentAlpha(), blueBase)
    }

    fun setRed() {
        setNewColor(currentAlpha(), redBase)
    }

    fun setYellow() {
        setNewColor(currentAlpha(), yellowBase)
    }

    fun setOrange() {
        setNewColor(currentAlpha(), orangeBase)
    }

    fun setRedYellow() {
        setNewColor(currentAlpha(), redYellowBase)
    }

    fun setGreen() {
        setNewColor(currentAlpha(), greenBase)
    }

    fun setDark() {
        setNewColor(currentAlpha(), darkBase)
    }

    fun setAlpha(alpha: Int) {
        setNewColor(alpha, preferenceApplier.filterColor(DEFAULT_COLOR))
    }

    fun setDefault() {
        setNewColor(DEFAULT_ALPHA, yellowBase)
    }

    private fun currentAlpha(): Int = android.graphics.Color.alpha(preferenceApplier.filterColor(DEFAULT_COLOR))

    private fun setNewColor(alpha: Int, @ColorInt newBaseColor: Int) {
        val newColor = ColorUtils.setAlphaComponent(newBaseColor, alpha)
        preferenceApplier.setFilterColor(newColor)
        contentViewModel?.refresh()
    }

    companion object {

        private val DEFAULT_COLOR = Color.Transparent.toArgb()

        private const val DEFAULT_ALPHA = 34

        fun getDefaultAlpha() = DEFAULT_ALPHA

    }

}