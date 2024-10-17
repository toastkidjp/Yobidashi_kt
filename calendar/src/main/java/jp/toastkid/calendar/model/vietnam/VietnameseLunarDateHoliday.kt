/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.vietnam

import jp.toastkid.calendar.model.holiday.Holiday
import jp.toastkid.calendar.model.holiday.HolidayCalendar
import java.util.Calendar

enum class VietnameseLunarDateHoliday(val month: Int, val date: Int, val title: String) {

    TET_1(1, 1, "Tet"),
    TET_2(1, 2, "Tet"),
    TET_3(1, 3, "Tet"),
    HUNG_KINGS_COMMEMORATIONS(3, 10, "Hung Kings Commemoration");

    companion object {

        private val TARGET_MONTHS = setOf(1, 2, 3, 4)

        private val lunarDateConverter = VietnamLunarDateConverter()

        fun find(year: Int, month: Int): List<Holiday> {
            if (TARGET_MONTHS.contains(month).not()) {
                return emptyList()
            }

            return entries
                .map {
                    val lunarDateToSolar =
                        lunarDateConverter.lunarDateToSolar(year, it.month, it.date)
                    Holiday(
                        it.title,
                        lunarDateToSolar.get(Calendar.MONTH) + 1,
                        lunarDateToSolar.get(Calendar.DAY_OF_MONTH),
                        HolidayCalendar.VIETNAM.flag
                    )
                }
                .filter { it.month == month }
        }

    }

}