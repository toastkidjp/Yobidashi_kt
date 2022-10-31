/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MainSnackbar(it: SnackbarData, onDismiss: () -> Unit) {
    val dismissSnackbarDistance = with(LocalDensity.current) { 72.dp.toPx() }
    val snackbarSwipingAnchors = mapOf(-dismissSnackbarDistance to -1, 0f to 0, dismissSnackbarDistance to 1)
    val snackbarSwipeableState = SwipeableState(
        initialValue = 0,
        confirmStateChange = {
            if (it == -1 || it == 1) {
                onDismiss()
            }
            true
        }
    )

    Snackbar(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 4.dp,
        modifier = Modifier
            .swipeable(
                snackbarSwipeableState,
                anchors = snackbarSwipingAnchors,
                thresholds = { _, _ -> FractionalThreshold(0.75f) },
                resistance = ResistanceConfig(0.5f),
                orientation = Orientation.Horizontal
            )
            .offset {
                IntOffset(
                    snackbarSwipeableState.offset.value.toInt(),
                    0
                )
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                it.message,
                modifier = Modifier.weight(1f)
            )
            if (it.actionLabel != null) {
                Text(
                    it.actionLabel ?: "",
                    modifier = Modifier
                        .clickable {
                            it.performAction()
                        }
                        .wrapContentWidth()
                        .padding(start = 4.dp)
                )
            }
        }
    }
}