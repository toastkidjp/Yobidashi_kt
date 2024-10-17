/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.holiday

import jp.toastkid.calendar.R
import jp.toastkid.calendar.service.OffDayFinderService
import jp.toastkid.calendar.service.japan.JapaneseOffDayFinderService
import jp.toastkid.calendar.service.uk.UKOffDayFinder
import jp.toastkid.calendar.service.us.AmericanOffDayFinderService
import jp.toastkid.calendar.service.vietnam.VietnameseOffDayCalculator

enum class HolidayCalendar(
    val settingTitleId: Int,
    private val offDayFinderService: OffDayFinderService,
    val flag: String = "\uD83C\uDDFA\uD83C\uDDF8"
    ) {

    JAPAN(R.string.title_use_ja_calendar, JapaneseOffDayFinderService(), "\uD83C\uDDEF\uD83C\uDDF5"),
    UK(R.string.title_use_uk_calendar, UKOffDayFinder(), "\uD83C\uDDEC\uD83C\uDDE7"),
    USA(R.string.title_use_us_calendar, AmericanOffDayFinderService()),
    VIETNAM(R.string.title_use_vn_calendar, VietnameseOffDayCalculator(), "\uD83C\uDDFB\uD83C\uDDF3");

    fun getHolidays(year: Int, month: Int): List<Holiday> {
        return offDayFinderService.invoke(
            year,
            month
        )
    }

    companion object {
        fun findByName(name: String?) = entries.firstOrNull { it.name == name }
    }

}