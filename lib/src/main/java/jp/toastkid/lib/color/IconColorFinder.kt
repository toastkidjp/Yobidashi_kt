/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.color

import android.content.Context
import android.content.res.Configuration
import android.view.View
import jp.toastkid.lib.night.DisplayMode
import jp.toastkid.lib.preference.PreferenceApplier

class IconColorFinder(
        private val configuration: Configuration,
        private val preferenceApplier: PreferenceApplier
) {

    operator fun invoke(): Int {
        val nightMode = DisplayMode(configuration).isNightMode() || preferenceApplier.useDarkMode()
        return if (nightMode) preferenceApplier.fontColor else preferenceApplier.color
    }

    companion object {

        fun from(context: Context) =
                from(context.resources.configuration, PreferenceApplier(context))

        fun from(view: View) =
                from(view.resources.configuration, PreferenceApplier(view.context))

        private fun from(configuration: Configuration, preferenceApplier: PreferenceApplier) =
                IconColorFinder(configuration, preferenceApplier)
    }

}