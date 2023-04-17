/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.appbar

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import jp.toastkid.todo.R

@Composable
internal fun AppBarUi(onClickAdd: () -> Unit) {
    Button(
        onClick = onClickAdd,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = Color.LightGray
        )
    ) {
        Text(text = stringResource(id = R.string.add))
    }
}