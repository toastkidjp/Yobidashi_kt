/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service

import java.util.Calendar
import java.util.GregorianCalendar

class DateCalculator {

    private val DAYS_OF_WEEK_FOR_LAST_WEEK =
        setOf(Calendar.MONDAY, Calendar.SUNDAY, Calendar.SATURDAY)

    operator fun invoke(year: Int, month: Int, week: Int): Int {
        val localDate = GregorianCalendar(year, month - 1, 1)
        val dayOfWeek = localDate.get(Calendar.DAY_OF_WEEK)
        val d = if (dayOfWeek == Calendar.MONDAY) {
            1
        } else {
            Calendar.SUNDAY - (dayOfWeek - 2)
        }

        val targetWeek = when {
            (week != -1) -> week
            DAYS_OF_WEEK_FOR_LAST_WEEK.contains(dayOfWeek) -> 5
            else -> 4
        }

        val offset = if (dayOfWeek <= Calendar.MONDAY) {
            1
        } else {
            0
        }
        return d + (7 * (targetWeek - offset))
    }
}