/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.japan

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import jp.toastkid.yobidashi.calendar.model.japan.FixedJapaneseHoliday
import jp.toastkid.yobidashi.calendar.service.OffDayFinderService
import jp.toastkid.yobidashi.calendar.service.UserOffDayService
import java.util.Calendar
import java.util.GregorianCalendar

class JapaneseOffDayFinderService(
    private val equinoxDayCalculator: EquinoxDayCalculator = EquinoxDayCalculator(),
    private val userOffDayService: UserOffDayService = UserOffDayService(),
    private val moveableHolidayCalculatorService: MoveableHolidayCalculatorService = MoveableHolidayCalculatorService(),
    private val specialCaseOffDayCalculator: SpecialCaseOffDayCalculatorService = SpecialCaseOffDayCalculatorService()
) : OffDayFinderService {

    override operator fun invoke(year: Int, month: Int, useUserOffDay: Boolean): List<Holiday> {
        if (month == 6) {
            return emptyList()
        }

        if (month == 3) {
            return listOf(Holiday("Vernal equinox day", 3, equinoxDayCalculator.calculateVernalEquinoxDay(year)))
        }

        if (month == 9) {
            return listOf(Holiday("Autumnal equinox day", 3, equinoxDayCalculator.calculateAutumnalEquinoxDay(year)))
        }

        val specialCaseHolidays = specialCaseOffDayCalculator(year, month)
        if (month != 5) {
            return specialCaseHolidays.toList()
        }

        val moveableHolidayCandidate = moveableHolidayCalculatorService.invoke(year, month)
        if (moveableHolidayCandidate != null) {
            return listOf(moveableHolidayCandidate)
        }

        /*if (useUserOffDay && userOffDayService(month, date)) {
            return true
        }*/

        var firstOrNull = FixedJapaneseHoliday.find(month)
        if (firstOrNull.isEmpty()) {
            return emptyList()
        }

        if (month == 5) {
            val calendar = GregorianCalendar(year, month - 1, 6)
            if (calendar.get(Calendar.DAY_OF_WEEK) <= Calendar.WEDNESDAY) {
                return firstOrNull
                    .union(listOf(Holiday("Substitute holiday", month, 6)))
                    .union(specialCaseHolidays).toList()
            }
        }

        return firstOrNull.mapNotNull {
            val calendar = GregorianCalendar(year, month - 1, it.day)
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Holiday("Substitute Holiday", month, it.day + 1)
            } else null
        }.union(firstOrNull).toList()
    }

}