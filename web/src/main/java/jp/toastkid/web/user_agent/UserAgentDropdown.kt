/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.user_agent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier

@Composable
fun UserAgentDropdown(expanded: Boolean, onDismissRequest: () -> Unit, onSelect: (UserAgent) -> Unit) {
    val current = PreferenceApplier(LocalContext.current).userAgent()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
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
                            colors = RadioButtonDefaults
                                .colors(selectedColor = MaterialTheme.colorScheme.secondary),
                            onClick = {
                                onSelect(userAgent)
                                onDismissRequest()
                            }
                        )
                    }
                },
                onClick = {
                    onSelect(userAgent)
                    onDismissRequest()
                }
            )
        }
    }
}