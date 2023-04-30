/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.japan

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import jp.toastkid.yobidashi.calendar.model.japan.MoveableJapaneseHoliday
import java.util.Calendar
import java.util.GregorianCalendar

class MoveableHolidayCalculatorService {

    operator fun invoke(year: Int, month: Int): Holiday? {
        if (MoveableJapaneseHoliday.isTargetMonth(month).not() && month != 8) {
            return null
        }

        if ((year == 2020 || year == 2021) && (month == 7 || month == 10)) {
            return null
        }

        val localDate = GregorianCalendar(year, month - 1, 1)
        val targetDay = MoveableJapaneseHoliday.find(month) ?: return null
        val dayOfWeek = localDate.get(Calendar.DAY_OF_WEEK)
        val d = if (dayOfWeek == Calendar.MONDAY) {
            1
        } else {
            Calendar.SUNDAY - (dayOfWeek - 2)
        }

        val offset = if (dayOfWeek <= Calendar.MONDAY) {
            1
        } else {
            0
        }

        return Holiday(
            targetDay.title,
            month,
            d + (7 * (targetDay.week - offset)),
            "\uD83C\uDDEF\uD83C\uDDF5"
        )
    }

}