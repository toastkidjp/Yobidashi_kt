/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.world.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorldTimeView(modifier: Modifier = Modifier) {
    val viewModel = remember { WorldTimeViewModel() }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.graphicsLayer { alpha = 0.75f }
    ) {
        Box(Modifier.padding(8.dp)) {
            LazyColumn(state = viewModel.listState()) {
                items(viewModel.items(), { it.timeZoneId }) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            .animateItemPlacement()
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

    SideEffect {
        viewModel.start()
    }
}