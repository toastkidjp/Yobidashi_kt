/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.list

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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.swipeable
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissItem(
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier
        .padding(
            start = 16.dp,
            end = 16.dp,
            top = 2.dp,
            bottom = 2.dp
        ),
    dismissContent: @Composable RowScope.() -> Unit
) {
    val directions: Set<DismissDirection> = setOf(
        DismissDirection.EndToStart,
        DismissDirection.StartToEnd
    )

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        elevation = 4.dp
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
            Modifier.swipeable(
                state = state,
                anchors = anchors,
                thresholds = { from, to ->
                    val dir = getDismissDirection(from, to)
                    FixedThreshold(if (dir == DismissDirection.EndToStart) 3000000.dp else 30000.dp)
                },
                orientation = Orientation.Horizontal,
                enabled = state.currentValue == DismissValue.Default,
                reverseDirection = false,
                resistance = ResistanceConfig(
                    basis = width,
                    factorAtMin = minFactor,
                    factorAtMax = maxFactor
                )
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
                    .background(MaterialTheme.colors.surface)
            )
        }

    }
}

private fun getDismissDirection(from: DismissValue, to: DismissValue): DismissDirection? {
    return when {
        // settled at the default state
        from == to && from == DismissValue.Default -> null
        // has been dismissed to the end
        from == to && from == DismissValue.DismissedToEnd -> DismissDirection.StartToEnd
        // has been dismissed to the start
        from == to && from == DismissValue.DismissedToStart -> DismissDirection.EndToStart
        // is currently being dismissed to the end
        from == DismissValue.Default && to == DismissValue.DismissedToEnd -> DismissDirection.StartToEnd
        // is currently being dismissed to the start
        from == DismissValue.Default && to == DismissValue.DismissedToStart -> DismissDirection.EndToStart
        // has been dismissed to the end but is now animated back to default
        from == DismissValue.DismissedToEnd && to == DismissValue.Default -> DismissDirection.StartToEnd
        // has been dismissed to the start but is now animated back to default
        from == DismissValue.DismissedToStart && to == DismissValue.Default -> DismissDirection.EndToStart
        else -> null
    }
}