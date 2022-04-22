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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.ui.R

@Composable
fun SingleLineText(
    @StringRes textId: Int,
    onClick: () -> Unit
) {
    Text(
        text = stringResource(id = textId),
        fontSize = 16.sp,
        color = colorResource(id = R.color.black),
        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable(onClick = onClick)
    )
}