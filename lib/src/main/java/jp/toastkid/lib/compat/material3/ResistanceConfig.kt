/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.compat.material3

import androidx.compose.runtime.Immutable
import jp.toastkid.lib.compat.material3.SwipeableDefaults.resistanceConfig
import kotlin.math.PI
import kotlin.math.sin

/**
 * Specifies how resistance is calculated in [swipeableCompat].
 *
 * There are two things needed to calculate resistance: the resistance basis determines how much
 * overflow will be consumed to achieve maximum resistance, and the resistance factor determines
 * the amount of resistance (the larger the resistance factor, the stronger the resistance).
 *
 * The resistance basis is usually either the size of the component which [swipeableCompat] is applied
 * to, or the distance between the minimum and maximum anchors. For a constructor in which the
 * resistance basis defaults to the latter, consider using [resistanceConfig].
 *
 * You may specify different resistance factors for each bound. Consider using one of the default
 * resistance factors in [SwipeableDefaults]: `StandardResistanceFactor` to convey that the user
 * has run out of things to see, and `StiffResistanceFactor` to convey that the user cannot swipe
 * this right now. Also, you can set either factor to 0 to disable resistance at that bound.
 *
 * @param basis Specifies the maximum amount of overflow that will be consumed. Must be positive.
 * @param factorAtMin The factor by which to scale the resistance at the minimum bound.
 * Must not be negative.
 * @param factorAtMax The factor by which to scale the resistance at the maximum bound.
 * Must not be negative.
 */
@Immutable
class ResistanceConfig(
    /*@FloatRange(from = 0.0, fromInclusive = false)*/
    val basis: Float,
    /*@FloatRange(from = 0.0)*/
    val factorAtMin: Float = SwipeableDefaults.StandardResistanceFactor,
    /*@FloatRange(from = 0.0)*/
    val factorAtMax: Float = SwipeableDefaults.StandardResistanceFactor
) {
    fun computeResistance(overflow: Float): Float {
        val factor = if (overflow < 0) factorAtMin else factorAtMax
        if (factor == 0f) return 0f
        val progress = (overflow / basis).coerceIn(-1f, 1f)
        return basis / factor * sin(progress * PI.toFloat() / 2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResistanceConfig) return false

        if (basis != other.basis) return false
        if (factorAtMin != other.factorAtMin) return false
        if (factorAtMax != other.factorAtMax) return false

        return true
    }

    override fun hashCode(): Int {
        var result = basis.hashCode()
        result = 31 * result + factorAtMin.hashCode()
        result = 31 * result + factorAtMax.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResistanceConfig(basis=$basis, factorAtMin=$factorAtMin, factorAtMax=$factorAtMax)"
    }
}