/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.detail

import android.net.Uri
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream

@Composable
internal fun ImageDetailDialogUi(imageUri: Uri) {
    val inputStream = FileInputStream(File(imageUri.toString()))
    val exifInterface = ExifInterface(inputStream)

    val text = ExifInformationExtractorUseCase().invoke(exifInterface) ?: return
    inputStream.close()

    var openInformationDialog by remember { mutableStateOf(false) }

    if (openInformationDialog.not()) {
        return
    }

    AlertDialog(
        onDismissRequest = { openInformationDialog = false },
        text = { Text(text) },
        confirmButton = { openInformationDialog = false }
    )
}
