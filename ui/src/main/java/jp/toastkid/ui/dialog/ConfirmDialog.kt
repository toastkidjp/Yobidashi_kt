/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.toastkid.ui.R

@Composable
fun DestructiveChangeConfirmDialog(
    @StringRes titleId: Int,
    onDismissRequest: () -> Unit,
    onClickOk: () -> Unit
) {
    ConfirmDialog(
        stringResource(id = titleId),
        stringResource(id = R.string.message_confirmation),
        Color.Red,
        onDismissRequest,
        onClickOk
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    messageColor: Color = MaterialTheme.colorScheme.onSurface,
    onDismissRequest: () -> Unit,
    onClickOk: () -> Unit = {},
) {
    val color = MaterialTheme.colorScheme.onSurface

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = message,
                color = messageColor
            )
        },
        confirmButton = {
            Text(
                text = stringResource(id = jp.toastkid.lib.R.string.ok),
                color = color,
                modifier = Modifier
                    .clickable {
                        onClickOk()
                        onDismissRequest()
                    }
                    .padding(8.dp)
            )
        },
        dismissButton = {
            Text(
                text = stringResource(id = jp.toastkid.lib.R.string.cancel),
                color = color,
                modifier = Modifier
                    .clickable(onClick = onDismissRequest)
                    .padding(8.dp)
            )
        }
    )
}