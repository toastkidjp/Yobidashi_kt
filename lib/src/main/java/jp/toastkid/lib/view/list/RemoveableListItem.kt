/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import jp.toastkid.lib.compat.material3.DismissDirection
import jp.toastkid.lib.compat.material3.DismissState
import jp.toastkid.lib.compat.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import jp.toastkid.lib.compat.material3.FixedThreshold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import jp.toastkid.lib.compat.material3.ResistanceConfig
import androidx.compose.material3.Surface
import jp.toastkid.lib.compat.material3.SwipeableDefaults
import jp.toastkid.lib.compat.material3.swipeableCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItem(
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dismissContent: @Composable RowScope.() -> Unit
) {
    val directions: Set<DismissDirection> = setOf(
        DismissDirection.EndToStart,
        DismissDirection.StartToEnd
    )

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        shadowElevation = 4.dp
    ) {
        val width = LocalConfiguration.current.screenWidthDp.toFloat()
        val marginEnd = 60.dp.value
        val anchors = mutableMapOf(0f to DismissValue.Default)
        if (DismissDirection.StartToEnd in directions) {
            anchors += width to DismissValue.DismissedToEnd
        }
        if (DismissDirection.EndToStart in directions) {
            anchors += -width to DismissValue.DismissedToStart
        }

        val minFactor =
            if (DismissDirection.EndToStart in directions) SwipeableDefaults.StandardResistanceFactor
            else SwipeableDefaults.StiffResistanceFactor
        val maxFactor =
            if (DismissDirection.StartToEnd in directions) SwipeableDefaults.StandardResistanceFactor
            else SwipeableDefaults.StiffResistanceFactor

        val endOffset = remember { mutableStateOf(0f) }

        val state = remember {
            DismissState(
                initialValue = DismissValue.Default,
                confirmStateChange = {
                    endOffset.value < marginEnd && endOffset.value != 0f
                }
            )
        }

        Box(
            Modifier.swipeableCompat(
                state = state,
                anchors = anchors,
                thresholds = { _, _ ->
                    FixedThreshold(3000000.dp)
                },
                orientation = Orientation.Horizontal,
                enabled = state.currentValue == DismissValue.Default,
                reverseDirection = false,
                resistance = ResistanceConfig(
                    basis = width,
                    factorAtMin = minFactor,
                    factorAtMax = maxFactor
                ),
                velocityThreshold = 3000000.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                content = {
                    val scale by animateFloatAsState(
                        if (state.targetValue == DismissValue.Default) 0.75f else 1f
                    )

                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(60.dp)
                            .background(Color(0xFFDD4444))
                            .padding(horizontal = 20.dp)
                            .clickable {
                                coroutineScope.launch {
                                    state.dismiss(DismissDirection.EndToStart)
                                    onClickDelete()
                                    state.reset()
                                    endOffset.value = 0f
                                }
                            },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Localized description",
                            modifier = Modifier.scale(scale)
                        )
                    }
                },
                modifier = Modifier.matchParentSize()
            )

            val min = min(marginEnd, max(-state.offset.value, 0f))
            if (state.dismissDirection == DismissDirection.EndToStart
                && endOffset.value < min) {
                endOffset.value = min
            } else if (state.dismissDirection == DismissDirection.StartToEnd) {
                endOffset.value = min
            }

            Row(
                content = dismissContent,
                modifier = Modifier
                    .offset { IntOffset(state.offset.value.roundToInt(), 0) }
                    .padding(end = endOffset.value.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

    }
}
