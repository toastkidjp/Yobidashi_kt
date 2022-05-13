/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class DarkModeApplier {

    operator fun invoke(
            preferenceApplier: PreferenceApplier,
            context: Context
    ) {
        val currentTheme = Theme.extract(
                preferenceApplier,
                ContextCompat.getColor(context, R.color.editor_cursor),
                ContextCompat.getColor(context, R.color.light_blue_200_dd)
        )
        darkTheme.apply(preferenceApplier)

        val contentViewModel = (context as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
        }

        contentViewModel?.snackWithAction(
            "Apply dark mode.",
            context.getString(R.string.undo),
            { currentTheme.apply(preferenceApplier) }
        )
    }

    companion object {
        private val darkTheme = Theme(
                Color.parseColor("#DD000600"),
                Color.parseColor("#FFFDD835"),
                Color.parseColor("#DD000600"),
                Color.parseColor("#FFFDD835"),
                Color.parseColor("#FFFFAB91"),
                Color.parseColor("#FFFFAB91"),
                true
        )
    }
}