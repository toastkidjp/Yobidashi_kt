/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.world.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.world.domain.model.WorldTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldTimeView(modifier: Modifier = Modifier) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val viewModel = remember { WorldTimeViewModel() }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.graphicsLayer { alpha = 0.85f }
    ) {
        Box(Modifier.padding(8.dp)) {
            LazyColumn(state = viewModel.listState()) {
                items(viewModel.items(), WorldTime::timeZoneId) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            .animateItem()
                    ) {
                        Text(
                            viewModel.label(it.timeZone()),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(it.time, fontSize = 14.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        contentViewModel.replaceAppBarContent {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable(onClick = viewModel::openChooser).padding(horizontal = 8.dp)
                ) {
                    Text(
                        viewModel.currentTimezoneLabel(),
                    )
                    DropdownMenu(
                        viewModel.openingChooser(),
                        viewModel::closeChooser
                    ) {
                        viewModel.pickupTimeZone().forEach {
                            DropdownMenuItem(
                                {
                                    Text(viewModel.label(it))
                                },
                                {
                                    viewModel.choose(it)
                                }
                            )
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable(onClick = viewModel::openHourChooser).padding(start = 8.dp)
                ) {
                    Text(
                        viewModel.currentHour(),
                        fontSize = 24.sp
                    )
                    DropdownMenu(
                        viewModel.openingHourChooser(),
                        viewModel::closeHourChooser
                    ) {
                        (0..23).forEach {
                            DropdownMenuItem(
                                {
                                    Text("$it",
                                            fontSize = 24.sp,
                                    )
                                },
                                {
                                    viewModel.chooseHour(it)
                                }
                            )
                        }
                    }
                }

                Text(":",
                    fontSize = 24.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                            .align(Alignment.CenterVertically)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable {
                        viewModel.openMinuteChooser()
                    }
                ) {
                    Text(
                        viewModel.currentMinute(),
                        fontSize = 24.sp
                        )
                    DropdownMenu(
                        viewModel.openingMinuteChooser(),
                        viewModel::closeMinuteChooser
                    ) {
                        (0..59).forEach {
                            DropdownMenuItem(
                                {
                                    Text("$it",
                                        fontSize = 24.sp
                                    )
                                },
                                {
                                    viewModel.chooseMinute(it)
                                }
                            )
                        }
                    }
                }
            }
        }

        viewModel.start()
    }
}