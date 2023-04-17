/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.compat.material3

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Immutable

/**
 * Collects information about the ongoing swipe or animation in [swipeableCompat].
 *
 * To access this information, use [SwipeableState.progress].
 *
 * @param from The state corresponding to the anchor we are moving away from.
 * @param to The state corresponding to the anchor we are moving towards.
 * @param fraction The fraction that the current position represents between [from] and [to].
 * Must be between `0` and `1`.
 */
@Immutable
@ExperimentalMaterial3Api
class SwipeProgress<T>(
    val from: T,
    val to: T,
    /*@FloatRange(from = 0.0, to = 1.0)*/
    val fraction: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SwipeProgress<*>) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (fraction != other.fraction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from?.hashCode() ?: 0
        result = 31 * result + (to?.hashCode() ?: 0)
        result = 31 * result + fraction.hashCode()
        return result
    }

    override fun toString(): String {
        return "SwipeProgress(from=$from, to=$to, fraction=$fraction)"
    }
}
