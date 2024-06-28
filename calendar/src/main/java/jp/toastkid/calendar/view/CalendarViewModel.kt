/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.view

import jp.toastkid.calendar.model.Week
import jp.toastkid.lib.ContentViewModel
import java.util.Calendar
import java.util.GregorianCalendar

class CalendarViewModel {

    fun openDateArticle(
        contentViewModel: ContentViewModel,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        background: Boolean = false
    ) {
        contentViewModel.openDateArticle(year, monthOfYear, dayOfMonth, background)
    }

    fun isToday(target: Calendar, date: Int): Boolean {
        val today = Calendar.getInstance()
        return target.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && target.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == date
    }

    fun makeMonth(firstDay: Calendar): MutableList<Week> {
        var hasStarted1 = false
        val current1 = GregorianCalendar(
            firstDay.get(Calendar.YEAR),
            firstDay.get(Calendar.MONTH),
            firstDay.get(Calendar.DAY_OF_MONTH)
        )

        val weeks = mutableListOf<Week>()
        for (i in 0..5) {
            val w = Week()
            week.forEach { dayOfWeek ->
                if (hasStarted1.not() && dayOfWeek != firstDay.get(Calendar.DAY_OF_WEEK)) {
                    w.addEmpty()
                    return@forEach
                }
                hasStarted1 = true

                if (firstDay.get(Calendar.MONTH) != current1.get(Calendar.MONTH)) {
                    w.addEmpty()
                } else {
                    w.add(current1)
                }
                current1.add(Calendar.DAY_OF_MONTH, 1)
            }
            if (w.anyApplicableDate()) {
                weeks.add(w)
            }
        }
        return weeks
    }

    fun getDayOfWeekLabel(dayOfWeek: Int) =
        when (dayOfWeek) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }

    fun week() = week

}

private val week = arrayOf(
    Calendar.SUNDAY,
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.WEDNESDAY,
    Calendar.THURSDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY
)