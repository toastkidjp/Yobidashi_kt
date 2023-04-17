/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.compat.material3

import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * The directions in which a [SwipeToDismiss] can be dismissed.
 */
enum class DismissDirection {
    /**
     * Can be dismissed by swiping in the reading direction.
     */
    StartToEnd,

    /**
     * Can be dismissed by swiping in the reverse of the reading direction.
     */
    EndToStart
}

/**
 * Possible values of [DismissState].
 */
enum class DismissValue {
    /**
     * Indicates the component has not been dismissed yet.
     */
    Default,

    /**
     * Indicates the component has been dismissed in the reading direction.
     */
    DismissedToEnd,

    /**
     * Indicates the component has been dismissed in the reverse of the reading direction.
     */
    DismissedToStart
}

/**
 * State of the [SwipeToDismiss] composable.
 *
 * @param initialValue The initial value of the state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@ExperimentalMaterial3Api
class DismissState(
    initialValue: DismissValue,
    confirmStateChange: (DismissValue) -> Boolean = { true }
) : SwipeableState<DismissValue>(initialValue, confirmStateChange = confirmStateChange) {
    /**
     * The direction (if any) in which the composable has been or is being dismissed.
     *
     * If the composable is settled at the default state, then this will be null. Use this to
     * change the background of the [SwipeToDismiss] if you want different actions on each side.
     */
    val dismissDirection: DismissDirection?
        get() = if (offset.value == 0f) null else if (offset.value > 0f) DismissDirection.StartToEnd else DismissDirection.EndToStart

    /**
     * Whether the component has been dismissed in the given [direction].
     *
     * @param direction The dismiss direction.
     */
    fun isDismissed(direction: DismissDirection): Boolean {
        return currentValue == if (direction == DismissDirection.StartToEnd) DismissValue.DismissedToEnd else DismissValue.DismissedToStart
    }

    /**
     * Reset the component to the default position with animation and suspend until it if fully
     * reset or animation has been cancelled. This method will throw [CancellationException] if
     * the animation is interrupted
     *
     * @return the reason the reset animation ended
     */
    suspend fun reset() = animateTo(targetValue = DismissValue.Default)

    /**
     * Dismiss the component in the given [direction], with an animation and suspend. This method
     * will throw [CancellationException] if the animation is interrupted
     *
     * @param direction The dismiss direction.
     */
    suspend fun dismiss(direction: DismissDirection) {
        val targetValue = if (direction == DismissDirection.StartToEnd) DismissValue.DismissedToEnd else DismissValue.DismissedToStart
        animateTo(targetValue = targetValue)
    }

}
