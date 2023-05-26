/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.model.uk

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import jp.toastkid.yobidashi.calendar.model.holiday.HolidayCalendar
import jp.toastkid.yobidashi.calendar.service.DateCalculator

enum class MoveableUKHoliday(private val month: Int, val week: Int, val title: String) {
    EARLY_MAY_BANK_HOLIDAY(5, 1, "Early May Bank Holiday"),
    SPRING_BANK_HOLIDAY(5, -1, "Spring Bank Holiday"),
    SUMMER_BANK_HOLIDAY(8, -1, "Summer Bank Holiday")
    ;

    companion object {

        private val dateCalculator = DateCalculator()

        fun find(year: Int, month: Int): List<Holiday> = values().filter { it.month == month }.map {
            Holiday(
                it.title,
                month,
                dateCalculator.invoke(year, month, it.week),
                HolidayCalendar.UK.flag
            )
        }

    }
}