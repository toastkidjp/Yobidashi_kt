/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.list

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDismissItem(
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dismissContent: @Composable RowScope.() -> Unit
) {
    val dismissSnackbarDistance = with(LocalDensity.current) { -60.dp.toPx() }
    val anchors = DraggableAnchors {
        Start at 0f
        End at dismissSnackbarDistance
    }

    val marginEnd = 60.dp.value

    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = Start,
            anchors = anchors,
            positionalThreshold = { 120.dp.value },
            velocityThreshold = { 120.dp.value },
            animationSpec = spring(),
            confirmValueChange = { true }
        )
    }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        shadowElevation = 4.dp
    ) {
        Box(
            Modifier
                .anchoredDraggable(
                    anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                content = {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(60.dp)
                            .drawBehind { drawRect(Color(0xFFDD4444)) }
                            .padding(horizontal = 20.dp)
                            .clickable {
                                coroutineScope.launch {
                                    anchoredDraggableState.dispatchRawDelta(-600.dp.value)
                                    onClickDelete()
                                    anchoredDraggableState.snapTo(Start)
                                }
                            },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Localized description"
                        )
                    }
                },
                modifier = Modifier.matchParentSize()
            )

            val surfaceColor = MaterialTheme.colorScheme.surface
            Row(
                content = dismissContent,
                modifier = Modifier
                    .offset {
                        IntOffset(min(anchoredDraggableState.requireOffset(), marginEnd).toInt(), 0)
                    }
                    .drawBehind { drawRect(surfaceColor) }
            )
        }

    }
}
