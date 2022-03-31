/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.EditorFontSize
import jp.toastkid.yobidashi.settings.color.ColorChooserDialogFragment
import jp.toastkid.yobidashi.settings.view.ColorPaletteUi

@Composable
internal fun EditorSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)
    val contentViewModel = (activityContext as? FragmentActivity)?.let {
        ViewModelProvider(activityContext).get(ContentViewModel::class.java)
    }
    val backgroundColor = preferenceApplier.editorBackgroundColor()
    val fontColor = preferenceApplier.editorFontColor()

    val initialBgColor = backgroundColor
    val initialFontColor = fontColor

    val iconTint = Color(IconColorFinder.from(activityContext).invoke())

    val currentBackgroundColor =
        remember { mutableStateOf(Color(preferenceApplier.editorBackgroundColor())) }

    val currentFontColor =
        remember { mutableStateOf(Color(preferenceApplier.editorFontColor())) }

    val cursorColor =
        remember { mutableStateOf(Color(preferenceApplier.editorCursorColor(ContextCompat.getColor(activityContext, R.color.editor_cursor)))) }

    val highlightColor =
        remember { mutableStateOf(Color(preferenceApplier.editorHighlightColor(ContextCompat.getColor(activityContext, R.color.light_blue_200_dd)))) }

    val fontSize =
        remember { mutableStateOf(preferenceApplier.editorFontSize()) }

    val fontSizeOpen =
        remember { mutableStateOf(false) }

    MaterialTheme() {
        LazyColumn(
            modifier = Modifier
                .nestedScroll(rememberViewInteropNestedScrollConnection())
                .padding(start = 8.dp, end = 8.dp)
        ) {
            item {
                ColorPaletteUi(
                    currentBackgroundColor,
                    currentFontColor,
                    initialBgColor,
                    initialFontColor,
                    onCommit = {
                        preferenceApplier.setEditorBackgroundColor(currentBackgroundColor.value.toArgb())
                        preferenceApplier.setEditorFontColor(currentFontColor.value.toArgb())

                        contentViewModel?.snackShort(R.string.settings_color_done_commit)
                    },
                    onReset = {
                        preferenceApplier.setEditorBackgroundColor(initialBgColor)
                        preferenceApplier.setEditorFontColor(initialFontColor)

                        currentBackgroundColor.value = Color(initialBgColor)
                        currentFontColor.value = Color(initialFontColor)

                        contentViewModel?.snackShort(R.string.settings_color_done_reset)
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                ColorChooserMenu(
                    cursorColor,
                    R.drawable.ic_cursor_black,
                    R.string.title_cursor_color,
                    iconTint
                ) { showCursorColorSetting(activityContext, cursorColor) }
            }

            item {
                InsetDivider()
            }

            item {
                ColorChooserMenu(
                    highlightColor,
                    R.drawable.ic_highlight_black,
                    R.string.title_highlight_color,
                    iconTint
                ) { showCursorColorSetting(activityContext, highlightColor) }
            }

            item {
                InsetDivider()
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(48.dp)
                        .clickable(onClick = { fontSizeOpen.value = true })
                        .background(colorResource(id = R.color.setting_background))
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_edit),
                        tint = iconTint,
                        contentDescription = stringResource(id = R.string.title_font_size)
                    )

                    Text(
                        stringResource(id = R.string.title_font_size),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.search_category_spinner_width))
                            .padding(end = 8.dp)
                    ) {
                        Text("${fontSize.value}")
                        DropdownMenu(
                            expanded = fontSizeOpen.value,
                            onDismissRequest = { fontSizeOpen.value = false }
                        ) {
                            EditorFontSize.values().forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        preferenceApplier.setEditorFontSize(it.size)
                                        fontSize.value = it.size
                                        fontSizeOpen.value = false
                                    }
                                ) {
                                    Text(
                                        "${it.size}",
                                        color = colorResource(id = R.color.black),
                                        fontSize = it.size.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ColorChooserMenu(
    colorState: MutableState<Color>,
    @DrawableRes iconId: Int,
    @StringRes textId: Int,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .background(colorResource(id = R.color.setting_background))
    ) {
        Icon(
            painterResource(id = iconId),
            tint = iconTint,
            contentDescription = stringResource(id = textId)
        )

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        Button(
            {},
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = colorState.value,
                contentColor = Color.Transparent,
                disabledContentColor = Color.LightGray
            ),
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.search_category_spinner_width))
                .padding(end = 8.dp)
        ) {

        }
    }
}

fun showCursorColorSetting(context: Context, colorState: MutableState<Color>) {
    val fragmentActivity = context as? FragmentActivity
    val supportFragmentManager = fragmentActivity?.supportFragmentManager ?: return
    supportFragmentManager.setFragmentResultListener(
        "color",
        fragmentActivity,
        { key, result ->
            val color = result.getInt(key)
            PreferenceApplier(fragmentActivity).setEditorCursorColor(color)
            colorState.value = Color(color)
            supportFragmentManager.clearFragmentResult("color")
        }
    )

    ColorChooserDialogFragment.withCurrentColor(colorState.value.toArgb())
        .show(
            supportFragmentManager,
            ColorChooserDialogFragment::class.java.canonicalName
        )
}
