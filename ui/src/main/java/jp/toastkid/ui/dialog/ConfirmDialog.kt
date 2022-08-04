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
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.toastkid.ui.R

@Composable
fun DestructiveChangeConfirmDialog(
    visibleState: MutableState<Boolean>,
    @StringRes titleId: Int,
    onClickOk: () -> Unit
) {
    ConfirmDialog(
        visibleState,
        stringResource(id = titleId),
        stringResource(id = R.string.message_confirmation),
        Color.Red,
        onClickOk
    )
}

@Composable
fun ConfirmDialog(
    visibleState: MutableState<Boolean>,
    title: String,
    message: String,
    messageColor: Color = Color.Black,
    onClickOk: () -> Unit
) {
    val color = MaterialTheme.colors.onSurface

    if (visibleState.value.not()) {
        return
    }

    AlertDialog(
        onDismissRequest = { visibleState.value = false },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.h5
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
                text = stringResource(id = R.string.ok),
                color = color,
                modifier = Modifier
                    .clickable {
                        onClickOk()
                        visibleState.value = false
                    }
                    .padding(8.dp)
            )
        },
        dismissButton = {
            Text(
                text = stringResource(id = R.string.cancel),
                color = color,
                modifier = Modifier
                    .clickable {
                        visibleState.value = false
                    }
                    .padding(8.dp)
            )
        }
    )
}