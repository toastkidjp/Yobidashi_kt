/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.setting.R
import jp.toastkid.setting.application.background.load.LoadedAction
import jp.toastkid.setting.domain.service.DarkModeApplier
import jp.toastkid.setting.presentation.SwitchRow
import jp.toastkid.setting.presentation.WithIcon
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.parts.InsetDivider
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DisplaySettingUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)

    val files = remember { mutableStateListOf<File>().also { it.addAll(loadFileChunk(FilesDir(activityContext, BACKGROUND_DIR))) } }

    val displayEffectState = remember { mutableStateOf(PreferenceApplier(activityContext).showDisplayEffect()) }

    val addingLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        val targets = mutableListOf<Uri>()
        val clipData = it.data?.clipData
        val itemCount = clipData?.itemCount ?: 0
        (0 until itemCount).mapNotNull { clipData?.getItemAt(it)?.uri }.forEach { targets.add(it) }
        it.data?.data?.let { targets.add(it) }

        LoadedAction(
            targets,
            activityContext,
            contentViewModel,
            files::add,
            BACKGROUND_DIR
        )
            .invoke()
    }

    val openClearImagesDialog = remember { mutableStateOf(false) }

    Surface(shadowElevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { DarkModeApplier().invoke(PreferenceApplier(activityContext), activityContext) }
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_dark_mode_black),
                    tint = MaterialTheme.colorScheme.secondary,
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

            SwitchRow(
                textId = R.string.title_display_effect,
                clickable = {
                    val preferenceApplier = PreferenceApplier(activityContext)
                    preferenceApplier.switchShowDisplayEffect()
                    displayEffectState.value = preferenceApplier.showDisplayEffect()
                    contentViewModel.setShowDisplayEffect(displayEffectState.value)
                },
                booleanState = displayEffectState
            )

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        PreferenceApplier(activityContext).removeBackgroundImagePath()
                        contentViewModel.snackShort(R.string.message_reset_bg_image)
                    }
            ) {
                Icon(
                    painterResource(id = jp.toastkid.lib.R.drawable.ic_close_black),
                    tint = MaterialTheme.colorScheme.secondary,
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

            WithIcon(
                textId = jp.toastkid.lib.R.string.title_delete_all,
                clickable = {
                    openClearImagesDialog.value = true
                },
                iconId = jp.toastkid.lib.R.drawable.ic_clear_form,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { addingLauncher.launch(makePickImage()) }
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_image),
                    tint = MaterialTheme.colorScheme.secondary,
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
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = stringResource(id = R.string.add_background_image)
                )
            }

            if (files.isEmpty()) {
                return@Column
            }

            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(files, { it.absolutePath + it.lastModified().toString() }) { imageFile ->
                    Surface(shadowElevation = 4.dp, modifier = Modifier
                        .height(200.dp)
                        .weight(1f)
                        .padding(4.dp)
                        .animateItemPlacement()
                    ) {
                        Box(Modifier.clickable {
                            PreferenceApplier(activityContext).backgroundImagePath = imageFile.path
                            contentViewModel
                                .snackShort(R.string.message_change_background_image)
                        }) {
                            Column {
                                AsyncImage(
                                    model = imageFile,
                                    contentDescription = imageFile.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .height(152.dp)
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
                                painterResource(id = jp.toastkid.lib.R.drawable.ic_remove_circle),
                                contentDescription = stringResource(id = jp.toastkid.lib.R.string.delete),
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable {
                                        if (!imageFile.exists()) {
                                            contentViewModel
                                                .snackShort(R.string.message_cannot_found_image)
                                            return@clickable
                                        }
                                        val successRemove = imageFile.delete()
                                        if (!successRemove) {
                                            contentViewModel
                                                .snackShort(R.string.message_failed_image_removal)
                                            return@clickable
                                        }
                                        files.remove(imageFile)
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

    if (openClearImagesDialog.value) {
        DestructiveChangeConfirmDialog(
            jp.toastkid.lib.R.string.clear_all,
            onDismissRequest = { openClearImagesDialog.value = false },
            onClickOk = {
                FilesDir(activityContext, BACKGROUND_DIR).clean()
                files.clear()
                contentViewModel.snackShort(R.string.message_success_image_removal)
            }
        )
    }
}

private fun loadFileChunk(filesDir: FilesDir) =
    filesDir.listFiles().toList()

/**
 * Make pick image intent.
 * @return Intent
 */
private fun makePickImage(): Intent {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    intent.type = "image/*"
    return intent
}

/**
 * Background image dir.
 */
private const val BACKGROUND_DIR: String = "background_dir"
