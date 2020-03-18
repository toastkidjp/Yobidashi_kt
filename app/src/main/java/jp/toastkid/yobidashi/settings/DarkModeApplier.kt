/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.graphics.Color
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class DarkModeApplier {

    operator fun invoke(
            preferenceApplier: PreferenceApplier,
            parent: View
    ) {
        val currentTheme = Theme.extract(preferenceApplier)
        darkTheme.apply(preferenceApplier)
        Toaster.snackLong(
                parent,
                "Apply dark mode.",
                R.string.undo,
                View.OnClickListener {
                    currentTheme.apply(preferenceApplier)
                },
                preferenceApplier.colorPair()
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