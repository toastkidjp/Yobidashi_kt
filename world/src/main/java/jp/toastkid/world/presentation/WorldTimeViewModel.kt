/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.world.presentation

import android.icu.text.DecimalFormat
import android.text.format.DateFormat
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AtomicReference
import jp.toastkid.world.domain.model.WorldTime
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class WorldTimeViewModel {

    private val currentTime = AtomicReference(Calendar.getInstance())

    private val listState = LazyListState()

    private val items = mutableStateListOf<WorldTime>()

    fun listState() = listState

    fun items(): List<WorldTime> = items

    private val pickupTimeZone = listOf(
        "Asia/Tokyo",
        "UTC",
        "America/New_York",
        "US/Pacific",
        "US/Hawaii",
        "Europe/Rome",
        "Australia/Sydney",
        "NZ",
        "Asia/Ho_Chi_Minh",
        "Asia/Ulaanbaatar",
        "Asia/Tbilisi",
        "Africa/Johannesburg",
        "America/Asuncion",
        "America/Buenos_Aires",
        "Pacific/Palau",
        "IST"
    )

    fun pickupTimeZone() = pickupTimeZone

    private val openChooser = mutableStateOf(false)

    fun openingChooser() = openChooser.value

    fun openChooser() {
        openChooser.value = true
    }

    fun closeChooser() {
        openChooser.value = false
    }

    fun choose(value: String) {
        val calendar = currentTime.get()
        calendar.timeZone = TimeZone.getTimeZone(value)
        currentTime.set(calendar)
        updateItems()
        closeChooser()
    }

    private val openHourChooser = mutableStateOf(false)

    fun openingHourChooser() = openHourChooser.value

    fun openHourChooser() {
        openHourChooser.value = true
    }

    fun closeHourChooser() {
        openHourChooser.value = false
    }

    fun chooseHour(value: Int) {
        val calendar = currentTime.get()
        calendar.set(Calendar.HOUR_OF_DAY, value)
        currentTime.set(calendar)
        updateItems()
        closeHourChooser()
    }

    private val openMinutesChooser = mutableStateOf(false)

    fun openingMinuteChooser() = openMinutesChooser.value

    fun openMinuteChooser() {
        openMinutesChooser.value = true
    }

    fun closeMinuteChooser() {
        openMinutesChooser.value = false
    }

    fun chooseMinute(value: Int) {
        val calendar = currentTime.get()
        calendar.set(Calendar.MINUTE, value)
        currentTime.set(calendar)
        updateItems()
        closeMinuteChooser()
    }

    private val zeroPaddingFormatter = DecimalFormat("00")

    fun currentHour() = zeroPaddingFormatter.format(currentTime.get().get(Calendar.HOUR_OF_DAY))

    fun currentMinute() = zeroPaddingFormatter.format(currentTime.get().get(Calendar.MINUTE))

    fun currentTimezoneLabel(): String {
        val emoji = emoji(currentTime.get().timeZone.id)
        if (emoji.isNotEmpty()) {
            return emoji
        }

        return currentTime.get().timeZone.id
    }

    private val availableIDs = pickupTimeZone.plus(TimeZone.getAvailableIDs()).distinct()

    fun start() {
        updateItems()
    }

    private fun updateItems() {
        items.clear()
        val currentTime = currentTime.get()
        val calendar = GregorianCalendar(currentTime.timeZone)
        calendar.timeInMillis = currentTime.timeInMillis

        availableIDs.mapNotNull {
            calendar.timeZone = TimeZone.getTimeZone(it)
            WorldTime(
                it,
                DateFormat.format(
                    "yyyy-MM-dd(E) HH:mm:ss",
                    calendar
                ).toString()
            )
        }
            .forEach { items.add(it) }
    }

    fun label(timeZoneId: String): String {
        val emoji = emoji(timeZoneId)
        return if (emoji.isNotEmpty()) "$emoji ${timeZoneId}" else emoji
    }

    private fun emoji(timeZoneId: String): String {
        return when (timeZoneId) {
            "Asia/Tokyo" ->"\uD83C\uDDEF\uD83C\uDDF5"
            "UTC" -> "UTC"
            "America/New_York" -> "\uD83C\uDDFA\uD83C\uDDF8"
            "US/Pacific" -> "\uD83C\uDDFA\uD83C\uDDF8"
            "US/Hawaii" -> "\uD83C\uDDFA\uD83C\uDDF8"
            "Europe/Rome" -> "\uD83C\uDDEE\uD83C\uDDF9"
            "Australia/Sydney" -> "\uD83C\uDDE6\uD83C\uDDFA"
            "NZ" -> "\uD83C\uDDF3\uD83C\uDDFF"
            "Asia/Ho_Chi_Minh" -> "\uD83C\uDDFB\uD83C\uDDF3"
            "Asia/Ulaanbaatar" -> "\uD83C\uDDF2\uD83C\uDDF3"
            "Asia/Tbilisi" -> "\uD83C\uDDEC\uD83C\uDDEA"
            "Africa/Johannesburg" -> "\uD83C\uDDFF\uD83C\uDDE6"
            "America/Asuncion" -> "\uD83C\uDDF5\uD83C\uDDFE"
            "America/Buenos_Aires" -> "\uD83C\uDDE6\uD83C\uDDF7"
            "Pacific/Palau" -> "\uD83C\uDDF5\uD83C\uDDFC"
            else -> ""
        }
    }

}