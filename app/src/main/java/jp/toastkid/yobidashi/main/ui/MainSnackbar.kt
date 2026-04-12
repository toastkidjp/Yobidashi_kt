/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MainSnackbar(snackbarData: SnackbarData, onDismiss: () -> Unit) {
    val dismissSnackbarDistance = with(LocalDensity.current) { 72.dp.toPx() }
    val anchors = DraggableAnchors {
        "Left" at -dismissSnackbarDistance
        "Start" at 0f
        "Right" at dismissSnackbarDistance
    }

    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = "Start",
            anchors = anchors,
        )
    }

    Snackbar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .anchoredDraggable(
                state = anchoredDraggableState,
                orientation = Orientation.Horizontal,
                reverseDirection = false,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                    anchoredDraggableState,
                    positionalThreshold = { dismissSnackbarDistance * 0.75f },
                    animationSpec = spring()
                ),
                overscrollEffect = object : OverscrollEffect {
                    override val isInProgress: Boolean
                        get() = anchoredDraggableState.currentValue == "Start"

                    override suspend fun applyToFling(
                        velocity: Velocity,
                        performFling: suspend (Velocity) -> Velocity
                    ) {
                        when (anchoredDraggableState.currentValue) {
                            "Right", "Left" -> onDismiss()
                            else -> anchoredDraggableState.snapTo("Start")
                        }
                    }

                    override fun applyToScroll(
                        delta: Offset,
                        source: NestedScrollSource,
                        performScroll: (Offset) -> Offset
                    ): Offset {
                        anchoredDraggableState.dispatchRawDelta(delta.x)
                        return delta
                    }

                }
            )
            .offset { IntOffset(anchoredDraggableState.requireOffset().toInt(), 0) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                snackbarData.visuals.message,
                modifier = Modifier.weight(1f)
            )
            if (snackbarData.visuals.actionLabel != null) {
                Text(
                    snackbarData.visuals.actionLabel ?: "",
                    modifier = Modifier
                        .clickable(onClick = snackbarData::performAction)
                        .wrapContentWidth()
                        .padding(start = 4.dp)
                )
            }
        }
    }
}