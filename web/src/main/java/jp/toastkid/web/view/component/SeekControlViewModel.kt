/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view.component

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SeekControlViewModel {

    private val openHourChooser = MutableStateFlow(false)

    fun openingHourChooser(): StateFlow<Boolean> = openHourChooser

    fun openHourChooser() {
        openHourChooser.value = true
    }

    fun closeHourChooser() {
        openHourChooser.value = false
    }

    private val hour = MutableStateFlow(0)

    fun hour() = hour.value

    fun setHour(newValue: Int) {
        hour.value = newValue
        closeHourChooser()
    }

    private val openMinuteChooser = MutableStateFlow(false)

    fun openingMinuteChooser(): StateFlow<Boolean> = openMinuteChooser

    fun openMinuteChooser() {
        openMinuteChooser.value = true
    }

    fun closeMinuteChooser() {
        openMinuteChooser.value = false
    }

    private val minute = MutableStateFlow(0)

    fun minute() = minute.value

    fun setMinute(newValue: Int) {
        minute.value = newValue
        closeMinuteChooser()
    }

    fun seekEvent(): SeekEvent? {
        return PresetSeekTarget.with(hour.value, minute.value).calculateOffset()
    }

    private val openPresetChooser = MutableStateFlow(false)

    fun openingPresetChooser(): StateFlow<Boolean> = openPresetChooser

    fun openPresetChooser() {
        openPresetChooser.value = true
    }

    fun closePresetChooser() {
        openPresetChooser.value = false
    }

    private val presetSeekTargets = listOf(
        PresetSeekTarget.fromText("11:17"),
            PresetSeekTarget.fromText("13:17"),
            PresetSeekTarget.fromText("15:20"),
            PresetSeekTarget.fromText("15:45")
    )

    fun presetSeekTargets() = presetSeekTargets

    private val relativeSeekEvents = listOf(
        "-1 min" to RelativeSeekEvent(hour = 0, minute = -1),
        "+1 min" to RelativeSeekEvent(hour = 0, minute = 1),
        "-10 min" to RelativeSeekEvent(hour = 0, minute = -10),
        "+10 min" to RelativeSeekEvent(hour = 0, minute = 10),
        "-1 hour" to RelativeSeekEvent(hour = -1, minute = 0),
        "+1 hour" to RelativeSeekEvent(hour = 1, minute = 0),
    )

    fun relativeSeekEvents() = relativeSeekEvents

}