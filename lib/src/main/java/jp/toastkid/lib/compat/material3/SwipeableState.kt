/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.compat.material3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlin.math.abs
import kotlin.math.sign

/**
 * State of the [swipeableCompat] modifier.
 *
 * This contains necessary information about any ongoing swipe or animation and provides methods
 * to change the state either immediately or by starting an animation. To create and remember a
 * [SwipeableState] with the default animation clock, use [rememberSwipeableState].
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Stable
open class SwipeableState<T>(
    initialValue: T,
    val animationSpec: AnimationSpec<Float> = SpringSpec(),
    val confirmStateChange: (newValue: T) -> Boolean = { true }
) {
    /**
     * The current value of the state.
     *
     * If no swipe or animation is in progress, this corresponds to the anchor at which the
     * [swipeableCompat] is currently settled. If a swipe or animation is in progress, this corresponds
     * the last anchor at which the [swipeableCompat] was settled before the swipe or animation started.
     */
    var currentValue: T by mutableStateOf(initialValue)
        private set

    /**
     * Whether the state is currently animating.
     */
    var isAnimationRunning: Boolean by mutableStateOf(false)
        private set

    /**
     * The current position (in pixels) of the [swipeableCompat].
     *
     * You should use this state to offset your content accordingly. The recommended way is to
     * use `Modifier.offsetPx`. This includes the resistance by default, if resistance is enabled.
     */
    val offset: State<Float> get() = offsetState

    /**
     * The amount by which the [swipeableCompat] has been swiped past its bounds.
     */
    val overflow: State<Float> get() = overflowState

    // Use `Float.NaN` as a placeholder while the state is uninitialised.
    private val offsetState = mutableStateOf(0f)
    private val overflowState = mutableStateOf(0f)

    // the source of truth for the "real"(non ui) position
    // basically position in bounds + overflow
    private val absoluteOffset = mutableStateOf(0f)

    // current animation target, if animating, otherwise null
    private val animationTarget = mutableStateOf<Float?>(null)

    var anchors by mutableStateOf(emptyMap<Float, T>())

    private val latestNonEmptyAnchorsFlow: Flow<Map<Float, T>> =
        snapshotFlow { anchors }
            .filter { it.isNotEmpty() }
            .take(1)

    var minBound = Float.NEGATIVE_INFINITY
    var maxBound = Float.POSITIVE_INFINITY

    fun ensureInit(newAnchors: Map<Float, T>) {
        if (anchors.isEmpty()) {
            // need to do initial synchronization synchronously :(
            val initialOffset = newAnchors.getOffset(currentValue)
            requireNotNull(initialOffset) {
                "The initial value must have an associated anchor."
            }
            offsetState.value = initialOffset
            absoluteOffset.value = initialOffset
        }
    }

    suspend fun processNewAnchors(
        oldAnchors: Map<Float, T>,
        newAnchors: Map<Float, T>
    ) {
        if (oldAnchors.isEmpty()) {
            // If this is the first time that we receive anchors, then we need to initialise
            // the state so we snap to the offset associated to the initial value.
            minBound = newAnchors.keys.minOrNull()!!
            maxBound = newAnchors.keys.maxOrNull()!!
            val initialOffset = newAnchors.getOffset(currentValue)
            requireNotNull(initialOffset) {
                "The initial value must have an associated anchor."
            }
            snapInternalToOffset(initialOffset)
        } else if (newAnchors != oldAnchors) {
            // If we have received new anchors, then the offset of the current value might
            // have changed, so we need to animate to the new offset. If the current value
            // has been removed from the anchors then we animate to the closest anchor
            // instead. Note that this stops any ongoing animation.
            minBound = Float.NEGATIVE_INFINITY
            maxBound = Float.POSITIVE_INFINITY
            val animationTargetValue = animationTarget.value
            // if we're in the animation already, let's find it a new home
            val targetOffset = if (animationTargetValue != null) {
                // first, try to map old state to the new state
                val oldState = oldAnchors[animationTargetValue]
                val newState = newAnchors.getOffset(oldState)
                // return new state if exists, or find the closes one among new anchors
                newState ?: newAnchors.keys.minByOrNull { abs(it - animationTargetValue) }!!
            } else {
                // we're not animating, proceed by finding the new anchors for an old value
                val actualOldValue = oldAnchors[offset.value]
                val value = if (actualOldValue == currentValue) currentValue else actualOldValue
                newAnchors.getOffset(value) ?: newAnchors
                    .keys.minByOrNull { abs(it - offset.value) }!!
            }
            try {
                animateInternalToOffset(targetOffset, animationSpec)
            } catch (c: CancellationException) {
                // If the animation was interrupted for any reason, snap as a last resort.
                snapInternalToOffset(targetOffset)
            } finally {
                currentValue = newAnchors.getValue(targetOffset)
                minBound = newAnchors.keys.minOrNull()!!
                maxBound = newAnchors.keys.maxOrNull()!!
            }
        }
    }

    var thresholds: (Float, Float) -> Float by mutableStateOf({ _, _ -> 0f })

    var velocityThreshold by mutableStateOf(0f)

    var resistance: ResistanceConfig? by mutableStateOf(null)

    val draggableState = DraggableState {
        val newAbsolute = absoluteOffset.value + it
        val clamped = newAbsolute.coerceIn(minBound, maxBound)
        val overflow = newAbsolute - clamped
        val resistanceDelta = resistance?.computeResistance(overflow) ?: 0f
        offsetState.value = clamped + resistanceDelta
        overflowState.value = overflow
        absoluteOffset.value = newAbsolute
    }

    private suspend fun snapInternalToOffset(target: Float) {
        draggableState.drag {
            dragBy(target - absoluteOffset.value)
        }
    }

    private suspend fun animateInternalToOffset(target: Float, spec: AnimationSpec<Float>) {
        draggableState.drag {
            var prevValue = absoluteOffset.value
            animationTarget.value = target
            isAnimationRunning = true
            try {
                Animatable(prevValue).animateTo(target, spec) {
                    dragBy(this.value - prevValue)
                    prevValue = this.value
                }
            } finally {
                animationTarget.value = null
                isAnimationRunning = false
            }
        }
    }

    /**
     * The target value of the state.
     *
     * If a swipe is in progress, this is the value that the [swipeableCompat] would animate to if the
     * swipe finished. If an animation is running, this is the target value of that animation.
     * Finally, if no swipe or animation is in progress, this is the same as the [currentValue].
     */
    val targetValue: T
        get() {
            // TODO(calintat): Track current velocity (b/149549482) and use that here.
            val target = animationTarget.value ?: computeTarget(
                offset = offset.value,
                lastValue = anchors.getOffset(currentValue) ?: offset.value,
                anchors = anchors.keys,
                thresholds = thresholds,
                velocity = 0f,
                velocityThreshold = Float.POSITIVE_INFINITY
            )
            return anchors[target] ?: currentValue
        }

    /**
     * Information about the ongoing swipe or animation, if any. See [SwipeProgress] for details.
     *
     * If no swipe or animation is in progress, this returns `SwipeProgress(value, value, 1f)`.
     */
    @ExperimentalMaterial3Api
    val progress: SwipeProgress<T>
        get() {
            val bounds = findBounds(offset.value, anchors.keys)
            val from: T
            val to: T
            val fraction: Float
            when (bounds.size) {
                0 -> {
                    from = currentValue
                    to = currentValue
                    fraction = 1f
                }
                1 -> {
                    from = anchors.getValue(bounds[0])
                    to = anchors.getValue(bounds[0])
                    fraction = 1f
                }
                else -> {
                    val (a, b) =
                        if (direction > 0f) {
                            bounds[0] to bounds[1]
                        } else {
                            bounds[1] to bounds[0]
                        }
                    from = anchors.getValue(a)
                    to = anchors.getValue(b)
                    fraction = (offset.value - a) / (b - a)
                }
            }
            return SwipeProgress(from, to, fraction)
        }

    /**
     * The direction in which the [swipeableCompat] is moving, relative to the current [currentValue].
     *
     * This will be either 1f if it is is moving from left to right or top to bottom, -1f if it is
     * moving from right to left or bottom to top, or 0f if no swipe or animation is in progress.
     */
    @ExperimentalMaterial3Api
    val direction: Float
        get() = anchors.getOffset(currentValue)?.let { sign(offset.value - it) } ?: 0f

    /**
     * Set the state without any animation and suspend until it's set
     *
     * @param targetValue The new target value to set [currentValue] to.
     */
    @ExperimentalMaterial3Api
    suspend fun snapTo(targetValue: T) {
        latestNonEmptyAnchorsFlow.collect { anchors ->
            val targetOffset = anchors.getOffset(targetValue)
            requireNotNull(targetOffset) {
                "The target value must have an associated anchor."
            }
            snapInternalToOffset(targetOffset)
            currentValue = targetValue
        }
    }

    /**
     * Set the state to the target value by starting an animation.
     *
     * @param targetValue The new value to animate to.
     * @param anim The animation that will be used to animate to the new value.
     */
    suspend fun animateTo(targetValue: T, anim: AnimationSpec<Float> = animationSpec) {
        latestNonEmptyAnchorsFlow.collect { anchors ->
            try {
                val targetOffset = anchors.getOffset(targetValue)
                requireNotNull(targetOffset) {
                    "The target value must have an associated anchor."
                }
                animateInternalToOffset(targetOffset, anim)
            } finally {
                val endOffset = absoluteOffset.value
                val endValue = anchors
                    // fighting rounding error once again, anchor should be as close as 0.5 pixels
                    .filterKeys { anchorOffset -> abs(anchorOffset - endOffset) < 0.5f }
                    .values.firstOrNull() ?: currentValue
                currentValue = endValue
            }
        }
    }

    /**
     * Perform fling with settling to one of the anchors which is determined by the given
     * [velocity]. Fling with settling [swipeableCompat] will always consume all the velocity provided
     * since it will settle at the anchor.
     *
     * In general cases, [swipeableCompat] flings by itself when being swiped. This method is to be
     * used for nested scroll logic that wraps the [swipeableCompat]. In nested scroll developer may
     * want to trigger settling fling when the child scroll container reaches the bound.
     *
     * @param velocity velocity to fling and settle with
     *
     * @return the reason fling ended
     */
    suspend fun performFling(velocity: Float) {
        latestNonEmptyAnchorsFlow.collect { anchors ->
            val lastAnchor = anchors.getOffset(currentValue)!!
            val targetValue = computeTarget(
                offset = offset.value,
                lastValue = lastAnchor,
                anchors = anchors.keys,
                thresholds = thresholds,
                velocity = velocity,
                velocityThreshold = velocityThreshold
            )
            val targetState = anchors[targetValue]
            if (targetState != null && confirmStateChange(targetState)) animateTo(targetState)
            // If the user vetoed the state change, rollback to the previous state.
            else animateInternalToOffset(lastAnchor, animationSpec)
        }
    }

    /**
     * Force [swipeableCompat] to consume drag delta provided from outside of the regular [swipeableCompat]
     * gesture flow.
     *
     * Note: This method performs generic drag and it won't settle to any particular anchor, *
     * leaving swipeable in between anchors. When done dragging, [performFling] must be
     * called as well to ensure swipeable will settle at the anchor.
     *
     * In general cases, [swipeableCompat] drags by itself when being swiped. This method is to be
     * used for nested scroll logic that wraps the [swipeableCompat]. In nested scroll developer may
     * want to force drag when the child scroll container reaches the bound.
     *
     * @param delta delta in pixels to drag by
     *
     * @return the amount of [delta] consumed
     */
    fun performDrag(delta: Float): Float {
        val potentiallyConsumed = absoluteOffset.value + delta
        val clamped = potentiallyConsumed.coerceIn(minBound, maxBound)
        val deltaToConsume = clamped - absoluteOffset.value
        if (abs(deltaToConsume) > 0) {
            draggableState.dispatchRawDelta(deltaToConsume)
        }
        return deltaToConsume
    }

    companion object {
        /**
         * The default [Saver] implementation for [SwipeableState].
         */
        fun <T : Any> Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (T) -> Boolean
        ) = Saver<SwipeableState<T>, T>(
            save = { it.currentValue },
            restore = { SwipeableState(it, animationSpec, confirmStateChange) }
        )
    }
}

// temp default nested scroll connection for swipeables which desire as an opt in
// revisit in b/174756744 as all types will have their own specific connection probably
val <T> SwipeableState<T>.PreUpPostDownNestedScrollConnection: NestedScrollConnection
    get() = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta < 0 && source == NestedScrollSource.Drag) {
                performDrag(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source == NestedScrollSource.Drag) {
                performDrag(available.toFloat()).toOffset()
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = Offset(available.x, available.y).toFloat()
            return if (toFling < 0 && offset.value > minBound) {
                performFling(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            performFling(velocity = Offset(available.x, available.y).toFloat())
            return available
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }


private fun computeTarget(
    offset: Float,
    lastValue: Float,
    anchors: Set<Float>,
    thresholds: (Float, Float) -> Float,
    velocity: Float,
    velocityThreshold: Float
): Float {
    val bounds = findBounds(offset, anchors)
    return when (bounds.size) {
        0 -> lastValue
        1 -> bounds[0]
        else -> {
            val lower = bounds[0]
            val upper = bounds[1]
            if (lastValue <= offset) {
                // Swiping from lower to upper (positive).
                if (velocity >= velocityThreshold) {
                    return upper
                } else {
                    val threshold = thresholds(lower, upper)
                    if (offset < threshold) lower else upper
                }
            } else {
                // Swiping from upper to lower (negative).
                if (velocity <= -velocityThreshold) {
                    return lower
                } else {
                    val threshold = thresholds(upper, lower)
                    if (offset > threshold) upper else lower
                }
            }
        }
    }
}

private fun <T> Map<Float, T>.getOffset(state: T): Float? {
    return entries.firstOrNull { it.value == state }?.key
}

/**
 * Create and [remember] a [SwipeableState] with the default animation clock.
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Composable
@ExperimentalMaterial3Api
fun <T : Any> rememberSwipeableState(
    initialValue: T,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (newValue: T) -> Boolean = { true }
): SwipeableState<T> {
    return rememberSaveable(
        saver = SwipeableState.Saver(
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    ) {
        SwipeableState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    }
}


/**
 *  Given an offset x and a set of anchors, return a list of anchors:
 *   1. [ ] if the set of anchors is empty,
 *   2. [ x' ] if x is equal to one of the anchors, accounting for a small rounding error, where x'
 *      is x rounded to the exact value of the matching anchor,
 *   3. [ min ] if min is the minimum anchor and x < min,
 *   4. [ max ] if max is the maximum anchor and x > max, or
 *   5. [ a , b ] if a and b are anchors such that a < x < b and b - a is minimal.
 */
private fun findBounds(
    offset: Float,
    anchors: Set<Float>
): List<Float> {
    // Find the anchors the target lies between with a little bit of rounding error.
    val a = anchors.filter { it <= offset + 0.001 }.maxOrNull()
    val b = anchors.filter { it >= offset - 0.001 }.minOrNull()

    return when {
        a == null ->
            // case 1 or 3
            listOfNotNull(b)
        b == null ->
            // case 4
            listOf(a)
        a == b ->
            // case 2
            // Can't return offset itself here since it might not be exactly equal
            // to the anchor, despite being considered an exact match.
            listOf(a)
        else ->
            // case 5
            listOf(a, b)
    }
}
