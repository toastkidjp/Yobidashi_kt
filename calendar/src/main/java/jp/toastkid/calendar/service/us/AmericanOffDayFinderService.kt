/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.service.us

import jp.toastkid.calendar.model.holiday.Holiday
import jp.toastkid.calendar.model.us.FixedAmericanHoliday
import jp.toastkid.calendar.model.us.MoveableAmericanHoliday
import jp.toastkid.calendar.service.OffDayFinderService
import java.util.Calendar
import java.util.GregorianCalendar

class AmericanOffDayFinderService : OffDayFinderService {

    override fun invoke(
        year: Int,
        month: Int,
        useUserOffDay: Boolean
    ): List<Holiday> {
        if (month == 3 || month == 4 || month == 8) {
            return emptyList()
        }

        val calendar = GregorianCalendar(year, month - 1, 1)

        val holidays = mutableListOf<Holiday>()
        MoveableAmericanHoliday.find(year, month)?.also {
            holidays.add(it)
        }

        FixedAmericanHoliday.find(year, month)?.let {
            holidays.add(it)

            calendar.set(Calendar.DAY_OF_MONTH, it.day)
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                holidays.add(Holiday("Substitute holiday", month, it.day + 1))
            }
        }
        return holidays

        /*if (useUserOffDay && userOffDayService(month, date)) {
            return true
        }*/
    }
}