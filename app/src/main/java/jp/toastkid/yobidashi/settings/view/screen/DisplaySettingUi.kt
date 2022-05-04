/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.DarkModeApplier
import jp.toastkid.yobidashi.settings.background.load.LoadedAction

/**
 * TODO implement clear setting menu.
 */
@Composable
internal fun DisplaySettingUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(activityContext)
    val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

    val filesDir = FilesDir(activityContext, BACKGROUND_DIR)

    val iconColor = Color(IconColorFinder.from(activityContext).invoke())

    val files = remember {
        mutableStateOf(loadFileChunk(filesDir))
    }

    val addingLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        LoadedAction(
            it.data?.data,
            activityContext,
            contentViewModel,
            { files.value = loadFileChunk(filesDir) },
            BACKGROUND_DIR
        )
            .invoke()
    }

    Surface(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        LazyColumn {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { DarkModeApplier().invoke(preferenceApplier, activityContext) }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_dark_mode_black),
                        tint = iconColor,
                        contentDescription = stringResource(id = R.string.apply_dark_mode)
                    )
                    Text(
                        text = stringResource(id = R.string.apply_dark_mode),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp)
                    )
                }

                InsetDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            preferenceApplier.removeBackgroundImagePath()
                            contentViewModel.snackShort(R.string.message_reset_bg_image)
                        }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_close_black),
                        tint = iconColor,
                        contentDescription = stringResource(id = R.string.title_bg_reset)
                    )
                    Text(
                        text = stringResource(id = R.string.title_bg_reset),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp)
                    )
                }

                InsetDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_image),
                        tint = iconColor,
                        contentDescription = stringResource(id = R.string.title_background_image_setting)
                    )
                    Text(
                        text = stringResource(id = R.string.title_background_image_setting),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                    Icon(
                        painterResource(id = R.drawable.ic_add_white),
                        tint = iconColor,
                        contentDescription = stringResource(id = R.string.add_background_image),
                        modifier = Modifier.clickable { addingLauncher.launch(makePickImage()) }
                    )
                }
            }

            if (files.value.isEmpty()) {
                return@LazyColumn
            }

            items(files.value) { column ->
                Row() {
                    column.forEach { imageFile ->
                        Surface(elevation = 4.dp, modifier = Modifier
                            .height(200.dp)
                            .weight(1f)
                            .padding(4.dp)) {
                            Box(Modifier.clickable {
                                preferenceApplier.backgroundImagePath = imageFile.path
                                contentViewModel
                                    ?.snackShort(R.string.message_change_background_image)
                            }) {
                                Column() {
                                    AsyncImage(
                                        model = imageFile,
                                        contentDescription = imageFile.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(150.dp)
                                            .padding(4.dp)
                                    )
                                    Text(
                                        imageFile.nameWithoutExtension,
                                        maxLines = 2,
                                        fontSize = 14.sp,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                                Icon(
                                    painterResource(id = R.drawable.ic_remove_circle),
                                    contentDescription = stringResource(id = R.string.delete),
                                    modifier = Modifier.size(40.dp).align(Alignment.TopEnd)
                                        .clickable {
                                            if (!imageFile.exists()) {
                                                contentViewModel
                                                    ?.snackShort(R.string.message_cannot_found_image)
                                                return@clickable
                                            }
                                            val successRemove = imageFile.delete()
                                            if (!successRemove) {
                                                contentViewModel
                                                    ?.snackShort(R.string.message_failed_image_removal)
                                                return@clickable
                                            }
                                            files.value = loadFileChunk(filesDir)
                                            contentViewModel
                                                ?.snackShort(R.string.message_success_image_removal)
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

private fun loadFileChunk(filesDir: FilesDir) =
    filesDir.listFiles().toList().windowed(2, 2, true)

    fun applyDarkMode() {
        /*view?.let {
            DarkModeApplier().invoke(preferenceApplier, it)
        }*/
    }

    /**
     * Make pick image intent.
     * @return Intent
     */
    private fun makePickImage(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        return intent
    }

    /**
     * Clear all images.
     */
    private fun clearImages() {
        /*TODO ConfirmDialogFragment.show(
            parentFragmentManager,
            getString(R.string.clear_all),
            Html.fromHtml(
                getString(R.string.confirm_clear_all_settings),
                Html.FROM_HTML_MODE_COMPACT
            ),
            "clear_background_images"
        )
        filesDir.clean()
                contentViewModel?.snackShort(R.string.message_success_image_removal)
                refresh()

        */
    }

    /*override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.background_settings_add_default -> {
            activity?.let { activity ->
                DefaultBackgroundImagePreparation().invoke(activity) {
                    activity.runOnUiThread { refresh() }
                }
            }
            true
        }
        R.id.background_settings_toolbar_menu_clear -> {
            clearImages()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }*/

/**
 * Background image dir.
 */
private const val BACKGROUND_DIR: String = "background_dir"

@StringRes
const val titleId: Int = R.string.title_settings_display
