/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.uk

import jp.toastkid.calendar.model.holiday.Holiday
import jp.toastkid.calendar.model.holiday.HolidayCalendar

enum class FixedUKHoliday(val month: Int, val date: Int, val title: String) {
    NEW_YEARS_DAY(1, 1, "New Year's Day"),
    CHRISTMAS_DAY(12, 25, "Christmas Day"),
    BOXING_DAY(12, 26, "Boxing Day")
    ;

    companion object {
        fun find(month: Int) = entries.filter { it.month == month }
            .map { Holiday(it.title, month, it.date, HolidayCalendar.UK.flag) }
    }
}