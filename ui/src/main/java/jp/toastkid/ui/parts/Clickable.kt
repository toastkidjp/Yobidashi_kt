/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.parts

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SingleLineText(
    @StringRes textId: Int,
    onClick: () -> Unit
) {
    Text(
        text = stringResource(id = textId),
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable(onClick = onClick)
    )
}

@Composable
fun WithIcon(
    textId: Int,
    clickable: () -> Unit,
    iconTint: Color,
    iconId: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = clickable)
            .padding(16.dp)
    ) {
        Icon(
            painterResource(id = iconId),
            tint = iconTint,
            contentDescription = stringResource(id = textId),
            modifier = Modifier.padding(end = 4.dp)
        )

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
        )
    }
}
