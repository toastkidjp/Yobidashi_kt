/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.setting.domain.service

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.setting.domain.model.Theme

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
                androidx.compose.ui.graphics.Color(0xDD81D4FA).toArgb(),
                androidx.compose.ui.graphics.Color(0xDD81D4FA).toArgb()
        )
        darkTheme.apply(preferenceApplier)

        val contentViewModel = (context as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
        }

        contentViewModel?.snackWithAction(
            "Apply dark mode.",
            context.getString(jp.toastkid.lib.R.string.undo),
            { currentTheme.apply(preferenceApplier) }
        )
    }

    companion object {
        private val darkTheme = Theme(
            "#DD000600".toColorInt(),
            "#FFFFC107".toColorInt(),
            "#DD000600".toColorInt(),
                Color.parseColor("#FFFFC107"),
                Color.parseColor("#DD81D4FA"),
                Color.parseColor("#DD81D4FA"),
                true
        )
    }
}