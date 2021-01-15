/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.night

import android.content.res.Configuration

/**
 *
 * @see https://developer.android.com/guide/topics/ui/look-and-feel/darktheme
 */
class DisplayMode(private val configuration: Configuration) {

    fun isNightMode(): Boolean {
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

}