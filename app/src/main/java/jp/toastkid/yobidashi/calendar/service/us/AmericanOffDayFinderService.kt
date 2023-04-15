/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.us

import jp.toastkid.yobidashi.calendar.model.us.FixedAmericanHoliday
import jp.toastkid.yobidashi.calendar.model.us.MoveableAmericanHoliday
import jp.toastkid.yobidashi.calendar.service.OffDayFinderService
import java.util.Calendar

class AmericanOffDayFinderService : OffDayFinderService {

    override fun invoke(
        year: Int,
        month: Int,
        date: Int,
        dayOfWeek: Int,
        useUserOffDay: Boolean
    ): Boolean {
        if (month == 3 || month == 4 || month == 8) {
            return false
        }

        if (MoveableAmericanHoliday.isHoliday(year, month, date)) {
            return true
        }

        /*if (useUserOffDay && userOffDayService(month, date)) {
            return true
        }*/

        var firstOrNull = FixedAmericanHoliday.values()
            .firstOrNull { month == it.month && date == it.date }
        if (firstOrNull == null) {
            if (dayOfWeek == Calendar.MONDAY) {
                firstOrNull = FixedAmericanHoliday.values()
                    .firstOrNull { month == it.month && (date - 1) == it.date }
            }
        }
        return firstOrNull != null
    }
}