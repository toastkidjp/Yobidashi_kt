/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model

import java.util.Calendar

class Week {
    private val days: MutableList<CalendarDate> = mutableListOf()

    fun add(date: Calendar) {
        days.add(CalendarDate(date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.DAY_OF_WEEK)))
    }

    fun addEmpty() {
        days.add(CalendarDate(-1, Calendar.MONDAY))
    }

    fun days() = days

    fun anyApplicableDate() = days.any { it.date != -1 }

}