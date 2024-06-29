/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.world.presentation

import android.text.format.DateFormat
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import jp.toastkid.world.domain.model.WorldTime
import java.util.Calendar
import java.util.TimeZone

class WorldTimeViewModel {

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
        "IST"
    )

    private val availableIDs = pickupTimeZone.plus(TimeZone.getAvailableIDs())

    fun start() {
        items.clear()

        availableIDs.mapNotNull {
            WorldTime(
                it,
                DateFormat.format(
                    "yyyy-MM-dd(E) HH:mm:ss",
                    Calendar.getInstance(TimeZone.getTimeZone(it))
                ).toString()
            )
        }
            .forEach { items.add(it) }
    }

    fun label(timeZoneId: String): String {
        return when (timeZoneId) {
            "Asia/Tokyo" ->"\uD83C\uDDEF\uD83C\uDDF5 Tokyo"
            "UTC" -> "UTC"
            "America/New_York" -> "\uD83C\uDDFA\uD83C\uDDF8 New York"
            "US/Pacific" -> "\uD83C\uDDFA\uD83C\uDDF8 US Pacific"
            "US/Hawaii" -> "Hawaii"
            "Europe/Rome" -> "\uD83C\uDDEE\uD83C\uDDF9 Rome"
            "Australia/Sydney" -> "\uD83C\uDDE6\uD83C\uDDFA Sydney"
            "NZ" -> "\uD83C\uDDF3\uD83C\uDDFF New Zealand"
            "Asia/Ho_Chi_Minh" -> "\uD83C\uDDFB\uD83C\uDDF3 Ho Chi Minh"
            else -> timeZoneId
        }
    }

}