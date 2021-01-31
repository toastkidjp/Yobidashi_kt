/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.color

import android.content.res.Configuration
import jp.toastkid.lib.night.DisplayMode
import jp.toastkid.lib.preference.PreferenceApplier

class IconColorFinder(
        private val configuration: Configuration,
        private val preferenceApplier: PreferenceApplier
) {

    operator fun invoke(): Int {
        val nightMode = DisplayMode(configuration).isNightMode()
        return if (nightMode) preferenceApplier.fontColor else preferenceApplier.color
    }

}