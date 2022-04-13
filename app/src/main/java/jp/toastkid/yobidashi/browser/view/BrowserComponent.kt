/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R

@Composable
internal fun BrowserTitle(
    progress: State<Int?>,
    headerTitle: State<String?>,
    headerUrl: State<String?>,
    modifier: Modifier
) {
    val context = LocalContext.current
    val tint = Color(PreferenceApplier(context).fontColor)

    Column(
        modifier = modifier
    ) {
        val progressTitle =
            if (progress.value ?: 100 < 70)
                context.getString(R.string.prefix_loading) + "${progress.value}%"
            else
                headerTitle.value ?: ""

        Text(
            text = progressTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = tint,
            fontSize = 12.sp
        )
        Text(
            text = headerUrl.value ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = tint,
            fontSize = 10.sp
        )
    }
}