package jp.toastkid.web.view.component

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BackwardSeekControlViewModel {

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

    fun seekEvent(): SeekEvent {
        return SeekEvent(hour.value, minute.value)
    }

}