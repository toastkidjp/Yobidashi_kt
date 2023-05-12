/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.vietnam

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import jp.toastkid.yobidashi.calendar.model.holiday.HolidayCalendar
import jp.toastkid.yobidashi.calendar.model.vietnam.FixedVietnamHoliday
import jp.toastkid.yobidashi.calendar.model.vietnam.VietnameseLunarDateHoliday
import jp.toastkid.yobidashi.calendar.service.OffDayFinderService
import java.util.Calendar
import java.util.GregorianCalendar

class VietnameseOffDayCalculator : OffDayFinderService {

    override fun invoke(year: Int, month: Int, useUserOffDay: Boolean): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        /*if (useUserOffDay && userOffDayService(month, date)) {
            return true
        }*/

        val firstOrNull = FixedVietnamHoliday.find(month)
        firstOrNull?.let {
            holidays.add(it)
        }

        val substitutes = holidays.mapNotNull {
            val calendar = GregorianCalendar(year, month - 1, it.day)
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY ->
                    Holiday("Substitute Holiday", month, it.day + 2, HolidayCalendar.VIETNAM.flag)
                Calendar.SUNDAY ->
                    Holiday("Substitute Holiday", month, it.day + 1, HolidayCalendar.VIETNAM.flag)
                else -> null
            }
        }

        holidays.addAll(VietnameseLunarDateHoliday.find(year, month))

        return substitutes.union(holidays).toList()
    }

}