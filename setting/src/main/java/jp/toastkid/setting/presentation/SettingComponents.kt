/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
internal fun CheckableRow(
    textId: Int,
    clickable: () -> Unit,
    booleanState: MutableState<Boolean>,
    iconTint: Color? = null,
    iconId: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = clickable)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        if (iconId != null && iconTint != null) {
            Icon(
                painterResource(id = iconId),
                tint = iconTint,
                contentDescription = stringResource(id = textId),
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
        )
        Checkbox(
            checked = booleanState.value, onCheckedChange = { clickable() },
            modifier = Modifier.width(44.dp)
        )
    }
}

@Composable
internal fun SwitchRow(
    textId: Int,
    clickable: () -> Unit,
    booleanState: MutableState<Boolean>,
    iconTint: Color? = null,
    iconId: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = clickable)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        if (iconId != null && iconTint != null) {
            Icon(
                painterResource(id = iconId),
                tint = iconTint,
                contentDescription = stringResource(id = textId),
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
        )
        Switch(
            checked = booleanState.value, onCheckedChange = { clickable() },
            modifier = Modifier.width(44.dp)
        )
    }
}