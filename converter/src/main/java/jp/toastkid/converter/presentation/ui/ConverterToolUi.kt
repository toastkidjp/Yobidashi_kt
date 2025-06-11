/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.toastkid.converter.domain.model.TatamiCountConverter
import jp.toastkid.converter.domain.model.UnixTimeConverter

@Composable
fun ConverterToolUi() {
    val converters = remember { listOf(TatamiCountConverter(), UnixTimeConverter()) }
    val currentIndex = remember { mutableIntStateOf(0) }
    val openChooser = remember { mutableStateOf(false) }

    Surface(
        shadowElevation = 4.dp
    ) {
        Column {
            Surface(
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.clickable { openChooser.value = true }) {
                    Text(converters[currentIndex.intValue].title())
                    DropdownMenu(expanded = openChooser.value, onDismissRequest = { openChooser.value = false }) {
                        converters.forEachIndexed { index, converter ->
                            DropdownMenuItem(
                                text = { Text(converter.title()) },
                                onClick = {
                                    currentIndex.intValue = index
                                    openChooser.value = false
                                }
                            )
                        }
                    }
                }
            }


            TwoValueConverterBox(converters[currentIndex.value])
        }
    }
}