/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.compat.material3

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CancellationException

class ModalBottomSheetState(
    initialValue: ModalBottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    internal val isSkipHalfExpanded: Boolean,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true }
) : SwipeableState<ModalBottomSheetValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {
    /**
     * Whether the bottom sheet is visible.
     */
    val isVisible: Boolean
        get() = currentValue != ModalBottomSheetValue.Hidden

    internal val hasHalfExpandedState: Boolean
        get() = anchors.values.contains(ModalBottomSheetValue.HalfExpanded)

    constructor(
        initialValue: ModalBottomSheetValue,
        animationSpec: AnimationSpec<Float> = SpringSpec(),
        confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true }
    ) : this(initialValue, animationSpec, isSkipHalfExpanded = false, confirmStateChange)

    init {
        if (isSkipHalfExpanded) {
            require(initialValue != ModalBottomSheetValue.HalfExpanded) {
                "The initial value must not be set to HalfExpanded if skipHalfExpanded is set to" +
                        " true."
            }
        }
    }

    /**
     * Show the bottom sheet with animation and suspend until it's shown. If the sheet is taller
     * than 50% of the parent's height, the bottom sheet will be half expanded. Otherwise it will be
     * fully expanded.
     *
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show() {
        val targetValue = when {
            hasHalfExpandedState -> ModalBottomSheetValue.HalfExpanded
            else -> ModalBottomSheetValue.Expanded
        }
        animateTo(targetValue = targetValue)
    }

    /**
     * Half expand the bottom sheet if half expand is enabled with animation and suspend until it
     * animation is complete or cancelled
     *
     * @throws [CancellationException] if the animation is interrupted
     */
    internal suspend fun halfExpand() {
        if (!hasHalfExpandedState) {
            return
        }
        animateTo(ModalBottomSheetValue.HalfExpanded)
    }

    /**
     * Fully expand the bottom sheet with animation and suspend until it if fully expanded or
     * animation has been cancelled.
     * *
     * @throws [CancellationException] if the animation is interrupted
     */
    internal suspend fun expand() = animateTo(ModalBottomSheetValue.Expanded)

    /**
     * Hide the bottom sheet with animation and suspend until it if fully hidden or animation has
     * been cancelled.
     *
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun hide() = animateTo(ModalBottomSheetValue.Hidden)

    internal val nestedScrollConnection = this.PreUpPostDownNestedScrollConnection

    companion object {
        /**
         * The default [Saver] implementation for [ModalBottomSheetState].
         */
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            skipHalfExpanded: Boolean,
            confirmStateChange: (ModalBottomSheetValue) -> Boolean
        ): Saver<ModalBottomSheetState, *> = Saver(
            save = { it.currentValue },
            restore = {
                ModalBottomSheetState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    isSkipHalfExpanded = skipHalfExpanded,
                    confirmStateChange = confirmStateChange
                )
            }
        )

        /**
         * The default [Saver] implementation for [ModalBottomSheetState].
         */
        @Deprecated(
            message = "Please specify the skipHalfExpanded parameter",
            replaceWith = ReplaceWith(
                "ModalBottomSheetState.Saver(" +
                        "animationSpec = animationSpec," +
                        "skipHalfExpanded = ," +
                        "confirmStateChange = confirmStateChange" +
                        ")"
            )
        )
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (ModalBottomSheetValue) -> Boolean
        ): Saver<ModalBottomSheetState, *> = Saver(
            animationSpec = animationSpec,
            skipHalfExpanded = false,
            confirmStateChange = confirmStateChange
        )
    }
}

/**
 * Create a [ModalBottomSheetState] and [remember] it.
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param skipHalfExpanded Whether the half expanded state, if the sheet is tall enough, should
 * be skipped. If true, the sheet will always expand to the [Expanded] state and move to the
 * [Hidden] state when hiding the sheet, either programmatically or by user interaction.
 * <b>Must not be set to true if the [initialValue] is [ModalBottomSheetValue.HalfExpanded].</b>
 * If supplied with [ModalBottomSheetValue.HalfExpanded] for the [initialValue], an
 * [IllegalArgumentException] will be thrown.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Composable
fun rememberModalBottomSheetState(
    initialValue: ModalBottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    skipHalfExpanded: Boolean,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true }
): ModalBottomSheetState {
    return rememberSaveable(
        initialValue, animationSpec, skipHalfExpanded, confirmStateChange,
        saver = ModalBottomSheetState.Saver(
            animationSpec = animationSpec,
            skipHalfExpanded = skipHalfExpanded,
            confirmStateChange = confirmStateChange
        )
    ) {
        ModalBottomSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            isSkipHalfExpanded = skipHalfExpanded,
            confirmStateChange = confirmStateChange
        )
    }
}

/**
 * Create a [ModalBottomSheetState] and [remember] it.
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Composable
fun rememberModalBottomSheetState(
    initialValue: ModalBottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true }
): ModalBottomSheetState = rememberModalBottomSheetState(
    initialValue = initialValue,
    animationSpec = animationSpec,
    skipHalfExpanded = false,
    confirmStateChange = confirmStateChange
)

/**
 * Possible values of [ModalBottomSheetState].
 */
enum class ModalBottomSheetValue {
    /**
     * The bottom sheet is not visible.
     */
    Hidden,

    /**
     * The bottom sheet is visible at full height.
     */
    Expanded,

    /**
     * The bottom sheet is partially visible at 50% of the screen height. This state is only
     * enabled if the height of the bottom sheet is more than 50% of the screen height.
     */
    HalfExpanded
}