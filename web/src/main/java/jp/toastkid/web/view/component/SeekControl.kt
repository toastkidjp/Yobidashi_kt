/*
 * Copyright (c) 2026toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.R

@Composable
fun SeekControl(
    onSeek: (SeekEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { SeekControlViewModel() }

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Text(
                    "${viewModel.hour()}",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(onClick = viewModel::openHourChooser)
                )

                DropdownMenu(
                    viewModel.openingHourChooser().collectAsState().value,
                    {
                        viewModel.closeHourChooser()
                    }
                ) {
                    (8..22).forEach { hour ->
                        DropdownMenuItem(
                            {
                                Text(
                                    "$hour",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            },
                            onClick = {
                                viewModel.setHour(hour)
                            }
                        )
                    }
                }
            }

            Text(":", modifier = Modifier.padding(4.dp))

            Box {
                Text(
                    "${viewModel.minute()}",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            viewModel.openMinuteChooser()
                        }
                )

                DropdownMenu(
                    viewModel.openingMinuteChooser().collectAsState().value,
                    {
                        viewModel.closeMinuteChooser()
                    }
                ) {
                    (0..59).forEach { minute ->
                        DropdownMenuItem(
                            {
                                Text(
                                    "$minute",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            },
                            onClick = {
                                viewModel.setMinute(minute)
                            }
                        )
                    }
                }
            }

            Button(
                {
                    viewModel.seekEvent()?.let(onSeek::invoke)
                }
            ) {
                Text(
                    "Seek",
                    fontSize = 16.sp
                )
            }

            Box(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_option_menu),
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Option",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            viewModel.openPresetChooser()
                        }
                )

                DropdownMenu(
                    viewModel.openingPresetChooser().collectAsState().value,
                    viewModel::closePresetChooser
                ) {
                    Text("Preset seek", modifier = Modifier.padding(4.dp))

                    viewModel.presetSeekTargets().forEach { preset ->
                        DropdownMenuItem(
                            {
                                Text(
                                    preset.text,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            },
                            onClick = {
                                preset.calculateOffset()?.let(onSeek::invoke)
                                viewModel.closePresetChooser()
                            }
                        )
                    }

                    viewModel.relativeSeekEvents().forEach {
                        DropdownMenuItem(
                            {
                                Text(
                                    it.first,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            },
                            onClick = {
                                onSeek(it.second)
                            }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setCurrentTime()
    }
}