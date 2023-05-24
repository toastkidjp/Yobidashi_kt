/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import jp.toastkid.yobidashi.R

@Composable
internal fun BrowserTitle(
    title: String?,
    url: String?,
    progress: Int,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        val progressTitle =
            if ((progress ?: 100) < 70)
                stringResource(id = R.string.prefix_loading) + "$progress%"
            else
                title ?: ""

        Text(
            text = progressTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp
        )
        Text(
            text = url ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp
        )
    }
}