/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.WindowOptionColorApplier

@Composable
fun AppTheme(
    colorPair: ColorPair,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val primary = Color(colorPair.bgColor())
    val onPrimary = Color(colorPair.fontColor())

    @SuppressLint("ConflictingOnColor")
    val colors =
        if (darkTheme)
            darkColorScheme(
                primary = primary,
                secondary = onPrimary,
                surface = Color(0xFF0F0F0F),
                background = Color(0xFF0F0F0F),
                onPrimary = onPrimary,
                onSurface = Color(0xFFF0F0F0),
                onBackground = Color(0xFFF0F0F0)
            )
        else
            lightColorScheme(
                primary = primary,
                secondary = primary,
                surface = Color(0xFFF0F0F0),
                background = Color(0xFFF0F0F0),
                onPrimary = onPrimary,
                onSurface = Color(0xFF000B00),
                onBackground = Color(0xFF000B00)
            )

    val handleColor = Color(PreferenceApplier(LocalContext.current).editorHighlightColor(Color(0xFF81D4FA).toArgb()))

    MaterialTheme(
        colorScheme = colors,
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

        val current = LocalContext.current
        val window = (current as? Activity)?.window
        LaunchedEffect(key1 = colorPair, block = {
            window?.let {
                WindowOptionColorApplier()(it, colorPair.bgColor())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    (current as? Activity)?.findViewById<View>(android.R.id.content)
                        ?.setBackgroundColor(colors.primary.toArgb())
                }
            }
        })
    }
}
