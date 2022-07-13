/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.WindowOptionColorApplier

@Composable
fun AppTheme(
    colorState: State<ColorPair>,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorPair = colorState.value
    val preferenceApplier = PreferenceApplier(LocalContext.current)

    (LocalContext.current as? Activity)?.window?.let {
        WindowOptionColorApplier()(it, colorPair)
    }

    val primary = Color(colorPair.bgColor())
    val onPrimary = Color(colorPair.fontColor())

    val colors =
        if (darkTheme)
            darkColors(
                primary = primary,
                surface = Color(0xFF0F0F0F),
                background = Color(0xFF0F0F0F),
                onPrimary = onPrimary,
                onSurface = Color(0xFFF0F0F0),
                onBackground = Color(0xFFF0F0F0)
            )
        else
            lightColors(
                primary = primary,
                onPrimary = onPrimary,
                surface = Color(0xFFF0F0F0),
                background = Color(0xFFF0F0F0),
                onSurface = Color(0xFF000B00),
                onBackground = Color(0xFF000B00)
            )

    val handleColor = Color(preferenceApplier.editorHighlightColor(Color(0xFF81D4FA).toArgb()))

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes()
    ) {
        CompositionLocalProvider(
            LocalTextSelectionColors.provides(
                TextSelectionColors(
                    handleColor = handleColor,
                    backgroundColor = handleColor.copy(alpha = 0.4f)
                )
            )
        ) {
            content()
        }
    }
}
