/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.user_agent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier

@Composable
fun UserAgentDropdown(open: MutableState<Boolean>, onSelect: (UserAgent) -> Unit) {
    val current = PreferenceApplier(LocalContext.current).userAgent()
    DropdownMenu(
        expanded = open.value,
        onDismissRequest = { open.value = false }
    ) {
        UserAgent.values().forEach { userAgent ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            userAgent.title(),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        )
                        RadioButton(
                            selected = userAgent.name == current,
                            onClick = {  }
                        )
                    }
                },
                onClick = {
                    onSelect(userAgent)
                    open.value = false
                }
            )
        }
    }
}