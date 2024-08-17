/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.model.OptionMenu

@Composable
fun OptionMenuItem(optionMenu: OptionMenu) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val title = stringResource(id = optionMenu.titleId)

        val iconId = optionMenu.iconId
        if (iconId != null) {
            Icon(
                painterResource(id = iconId),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Text(
            title,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )

        val checked = remember { mutableStateOf(optionMenu.check?.invoke()) }
        if (checked.value != null) {
            Checkbox(
                checked = checked.value ?: false,
                onCheckedChange = {
                    optionMenu.action()
                    checked.value = it
                }
            )
        }
    }
}