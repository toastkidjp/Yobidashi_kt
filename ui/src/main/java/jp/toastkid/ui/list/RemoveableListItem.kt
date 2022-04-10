/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissItem(
    dismissState: DismissState,
    dismissContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
        .padding(
            start = 16.dp,
            end = 16.dp,
            top = 2.dp,
            bottom = 2.dp
        )
) {
    Surface(
        modifier = modifier,
        elevation = 4.dp
    ) {
        SwipeToDismiss(
            state = dismissState,
            modifier = Modifier.padding(vertical = 4.dp),
            directions = setOf(
                DismissDirection.EndToStart
            ),
            dismissThresholds = { direction ->
                FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.95f)
            },
            background = {
                val direction =
                    dismissState.dismissDirection ?: return@SwipeToDismiss
                val color by animateColorAsState(
                    when (dismissState.targetValue) {
                        DismissValue.DismissedToStart -> Color(0xFFDD4444)
                        else -> Color(0x88DD4444)
                    }
                )
                val alignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                }
                val icon = when (direction) {
                    DismissDirection.StartToEnd -> null
                    DismissDirection.EndToStart -> Icons.Default.Delete
                }
                val scale by animateFloatAsState(
                    if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                )

                if (icon != null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            icon,
                            contentDescription = "Localized description",
                            modifier = Modifier.scale(scale)
                        )
                    }
                }
            },
            dismissContent = dismissContent
        )
    }
}