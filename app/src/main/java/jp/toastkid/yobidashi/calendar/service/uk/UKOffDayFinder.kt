/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.uk

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import jp.toastkid.yobidashi.calendar.model.uk.FixedUKHoliday
import jp.toastkid.yobidashi.calendar.model.uk.MoveableUKHoliday
import jp.toastkid.yobidashi.calendar.service.OffDayFinderService
import java.util.Calendar
import java.util.GregorianCalendar

class UKOffDayFinder : OffDayFinderService {

    override fun invoke(year: Int, month: Int, useUserOffDay: Boolean): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        /*if (useUserOffDay && userOffDayService(month, date)) {
            return true
        }*/

        val firstOrNull = FixedUKHoliday.find(month)
        holidays.addAll(firstOrNull)

        val substitutes = holidays.mapNotNull {
            val calendar = GregorianCalendar(year, month - 1, it.day)
            if (month != 5 && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Holiday("Substitute Holiday", month, it.day + 1, "\uD83C\uDDEC\uD83C\uDDE7")
            } else null
        }

        holidays.addAll(MoveableUKHoliday.find(year, month))
        holidays.addAll(EasterHolidayCalculator().invoke(year, month))

        return substitutes.union(holidays).toList()
    }

}