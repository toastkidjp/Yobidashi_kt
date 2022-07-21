/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.load

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import jp.toastkid.editor.R
import jp.toastkid.ui.list.SwipeToDismissItem
import timber.log.Timber
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun LoadFromStorageDialogUi(
    openDialog: MutableState<Boolean>,
    files: Array<File>?,
    onSelect: (File) -> Unit
) {
    files ?: return
    val fileItems = remember { mutableStateListOf<File>() }
    fileItems.addAll(files)

    Dialog(
        onDismissRequest = {
            openDialog.value = false
        }
    ) {
        Surface(elevation = 4.dp) {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_load),
                            contentDescription = stringResource(id = R.string.load_from_storage)
                        )
                        Text(
                            text = stringResource(id = R.string.load_from_storage),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                items(fileItems) { file ->
                    SwipeToDismissItem(
                        onClickDelete = {
                            try {
                                file.delete()
                                fileItems.remove(file)
                            } catch (e: IOException) {
                                Timber.e(e)
                            }
                        },
                        dismissContent = {
                            Text(
                                text = file.name,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clickable {
                                        onSelect(file)
                                        openDialog.value = false
                                    }
                                    .fillParentMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}