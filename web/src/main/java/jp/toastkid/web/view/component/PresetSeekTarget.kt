/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view.component

import java.util.Calendar
import java.util.concurrent.TimeUnit

data class PresetSeekTarget(
    val text: String,
    val hour: Int,
    val minute: Int
) {

    fun calculateOffset(current: Calendar = Calendar.getInstance()): SeekEvent? {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, current.get(Calendar.YEAR))
        calendar.set(Calendar.DAY_OF_YEAR, current.get(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, this.hour)
        calendar.set(Calendar.MINUTE, this.minute)

        val offsetMs = current.timeInMillis - calendar.timeInMillis
        if (offsetMs < 0) {
            return null
        }

        val hours: Long = TimeUnit.MILLISECONDS.toHours(offsetMs)
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(offsetMs) % 60
        return SeekEvent(hours.toInt(), minutes.toInt())
    }

    companion object {

        fun fromText(text: String): PresetSeekTarget {
            if (text.contains(":").not()) {
                return PresetSeekTarget(text, 0, 0)
            }

            val split = text.split(":")
            return PresetSeekTarget(text, split[0].toInt(), split[1].toInt())
        }

    }

}