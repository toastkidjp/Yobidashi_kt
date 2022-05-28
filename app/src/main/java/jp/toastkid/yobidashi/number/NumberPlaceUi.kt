/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.number

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel

@Composable
fun NumberPlaceUi() {
    val fontSize = 28.sp

    val viewModel = viewModel(NumberPlaceViewModel::class.java)

    val contentViewModel = (LocalContext.current as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

    Surface(
        elevation = 4.dp
    ) {
        Column() {
            viewModel.masked().rows().forEachIndexed { rowIndex, row ->
                Row() {
                    row.forEachIndexed { columnIndex, cellValue ->
                        if (cellValue == -1) {
                            val open = remember { mutableStateOf(false) }
                            val number = remember { mutableStateOf("_") }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        open.value = true
                                    }
                            ) {
                                Text(
                                    number.value,
                                    color = Color(0xFF444499),
                                    fontSize = fontSize
                                )
                                DropdownMenu(open.value, onDismissRequest = { open.value = false }) {
                                    (1..9).forEach {
                                        DropdownMenuItem(onClick = {
                                            number.value = "$it"
                                            open.value = false
                                            viewModel.place(rowIndex, columnIndex, it) { done ->
                                                contentViewModel?.snackShort(
                                                    if (done) "Well done!" else "Incorrect..."
                                                )
                                            }
                                        }) {
                                            Text(
                                                text = "$it",
                                                fontSize = fontSize
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                cellValue.toString(),
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
