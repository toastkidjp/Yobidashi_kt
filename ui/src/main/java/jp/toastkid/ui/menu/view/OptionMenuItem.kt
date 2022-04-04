/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.ui.R
import jp.toastkid.ui.menu.model.OptionMenu

@Composable
fun OptionMenuItem(optionMenu: OptionMenu) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val title = stringResource(id = optionMenu.titleId)
        val color = Color(IconColorFinder.from(LocalContext.current).invoke())

        if (optionMenu.iconId != null) {
            Icon(
                painterResource(id = optionMenu.iconId),
                contentDescription = title,
                tint = color
            )
        }

        Text(
            title,
            fontSize = 16.sp,
            color = colorResource(id = R.color.black),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )

        if (optionMenu.checkState != null) {
            Checkbox(
                checked = optionMenu.checkState.value,
                onCheckedChange = {},
                Modifier.clickable(false) {})
        }
    }
}