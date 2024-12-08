/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.view.refresh

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class SwipeRefreshNestedScrollConnection(
    private val state: PullToRefreshState,
    private val coroutineScope: CoroutineScope,
    private val refreshTrigger: Float,
    private val setRefreshing: () -> Unit
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If we're refreshing, return zero
        state.isAnimating -> Offset.Zero
        // If the user is swiping up, handle it
        source == NestedScrollSource.UserInput -> onScroll(available)
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (state.distanceFraction > 1.5f) {
            setRefreshing()
        }
        return when {
            // If we're refreshing, return zero
            state.isAnimating -> Offset.Zero
            // If the user is swiping down and there's y remaining, handle it
            source == NestedScrollSource.UserInput -> onScroll(available)
            else -> Offset.Zero
        }
    }

    private fun onScroll(available: Offset): Offset {
        val dragConsumed = (available.y * -0.4f)

        coroutineScope.launch {
            state.snapTo(dragConsumed / refreshTrigger)
        }
        return available
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // Don't consume any velocity, to allow the scrolling layout to fling
        return Velocity.Zero
    }

}