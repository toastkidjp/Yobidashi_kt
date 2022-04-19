/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import jp.toastkid.lib.preference.PreferenceApplier

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val preferenceApplier = PreferenceApplier(LocalContext.current)
    val colors =
        if (darkTheme)
            darkColors(
                primary = Color(preferenceApplier.color),
                surface = Color(0xFF0F0F0F),
                background = Color(0xFF0F0F0F),
                onPrimary = Color(preferenceApplier.fontColor),
                onSurface = Color(0xFFF0F0F0),
                onBackground = Color(0xFFF0F0F0)
            )
        else
            lightColors(
                primary = Color(preferenceApplier.color),
                onPrimary = Color(preferenceApplier.fontColor),
                surface = Color(0xFFF0F0F0),
                background = Color(0xFFF0F0F0),
                onSurface = Color(0xFF000B00),
                onBackground = Color(0xFF000B00)
            )

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
