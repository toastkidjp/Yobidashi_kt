/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.godaddy.android.colorpicker.ClassicColorPicker
import jp.toastkid.editor.EditorFontSize
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.view.ColorPaletteUi
import jp.toastkid.yobidashi.settings.view.WithIcon

@Composable
internal fun EditorSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = remember { PreferenceApplier(activityContext) }
    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, activityContext)
    }
    val backgroundColor = preferenceApplier.editorBackgroundColor()
    val fontColor = preferenceApplier.editorFontColor()
    
    val currentBackgroundColor =
        remember { mutableStateOf(Color(preferenceApplier.editorBackgroundColor())) }

    val currentFontColor =
        remember { mutableStateOf(Color(preferenceApplier.editorFontColor())) }

    val cursorColor =
        remember {
            val color = preferenceApplier.editorCursorColor(
                ContextCompat.getColor(activityContext, R.color.editor_cursor)
            )
            mutableStateOf(Color(color))
        }

    val highlightColor =
        remember {
            val color = preferenceApplier.editorHighlightColor(Color(0xDD81D4FA).toArgb())
            mutableStateOf(Color(color))
        }

    val fontSize =
        remember { mutableStateOf(preferenceApplier.editorFontSize()) }

    val fontSizeOpen =
        remember { mutableStateOf(false) }

    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
    ) {
        LazyColumn {
            item {
                ColorPaletteUi(
                    currentBackgroundColor,
                    currentFontColor,
                    backgroundColor,
                    fontColor,
                    onCommit = {
                        preferenceApplier.setEditorBackgroundColor(currentBackgroundColor.value.toArgb())
                        preferenceApplier.setEditorFontColor(currentFontColor.value.toArgb())

                        contentViewModel?.snackShort(R.string.settings_color_done_commit)
                    },
                    onReset = {
                        preferenceApplier.setEditorBackgroundColor(backgroundColor)
                        preferenceApplier.setEditorFontColor(fontColor)

                        currentBackgroundColor.value = Color(backgroundColor)
                        currentFontColor.value = Color(fontColor)

                        contentViewModel?.snackShort(R.string.settings_color_done_reset)
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_copy_colors,
                    {
                        preferenceApplier.setEditorBackgroundColor(preferenceApplier.color)
                        preferenceApplier.setEditorFontColor(preferenceApplier.fontColor)

                        currentBackgroundColor.value = Color(preferenceApplier.color)
                        currentFontColor.value = Color(preferenceApplier.fontColor)

                        contentViewModel?.snackShort(R.string.settings_color_done_commit)
                    },
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_clip
                )
            }

            item {
                InsetDivider()
            }

            item {
                ColorChooserMenu(
                    cursorColor,
                    R.drawable.ic_cursor_black,
                    R.string.title_cursor_color
                ) {
                    preferenceApplier.setEditorCursorColor(it.toArgb())
                    cursorColor.value = it
                }
            }

            item {
                InsetDivider()
            }

            item {
                ColorChooserMenu(
                    highlightColor,
                    R.drawable.ic_highlight_black,
                    R.string.title_highlight_color
                ) {
                    preferenceApplier.setEditorHighlightColor(it.toArgb())
                    highlightColor.value = it
                }
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
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_edit),
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = stringResource(id = R.string.title_font_size),
                        modifier = Modifier.padding(start = 16.dp)
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
                            .size(44.dp)
                            .padding(end = 16.dp)
                    ) {
                        Text("${fontSize.value}")
                        DropdownMenu(
                            expanded = fontSizeOpen.value,
                            onDismissRequest = { fontSizeOpen.value = false }
                        ) {
                            EditorFontSize.values().forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${it.size}",
                                            fontSize = it.size.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .padding(8.dp)
                                        )
                                    },
                                    onClick = {
                                        preferenceApplier.setEditorFontSize(it.size)
                                        fontSize.value = it.size
                                        fontSizeOpen.value = false
                                    }
                                )
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
    onNewColor: (Color) -> Unit
) {
    val openColorChooserDialog = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .padding(start = 16.dp, end = 16.dp)
            .clickable {
                openColorChooserDialog.value = true
            }
    ) {
        Icon(
            painterResource(id = iconId),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = stringResource(id = textId)
        )

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )

        Box(
            modifier = Modifier
                .background(colorState.value)
                .size(44.dp)
        ) { }
    }

    ComponentColorSettingDialog(
        openColorChooserDialog,
        colorState.value
    ) {
        onNewColor(it)
    }
}

@Composable
private fun ComponentColorSettingDialog(
    openColorChooserDialog: MutableState<Boolean>,
    currentColor: Color,
    onNewColor: (Color) -> Unit
) {
    if (openColorChooserDialog.value) {
        val choosingColor = remember { mutableStateOf(currentColor) }

        AlertDialog(
            onDismissRequest = { openColorChooserDialog.value = false },
            title = {
                Text(stringResource(id = R.string.title_dialog_color_chooser))
            },
            text = {
                ClassicColorPicker(
                    color = choosingColor.value,
                    onColorChanged = { hsvColor ->
                        choosingColor.value = hsvColor.toColor()
                    },
                    modifier = Modifier.height(200.dp)
                )
            },
            confirmButton = {
                Text(
                    text = stringResource(id = jp.toastkid.ui.R.string.ok),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable {
                            val newColor = choosingColor.value
                            onNewColor(newColor)
                            openColorChooserDialog.value = false
                        }
                        .padding(4.dp)
                )
            },
            dismissButton = {
                Text(
                    text = stringResource(id = jp.toastkid.ui.R.string.cancel),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable {
                            openColorChooserDialog.value = false
                        }
                        .padding(4.dp)
                )
            }
        )
    }
}
