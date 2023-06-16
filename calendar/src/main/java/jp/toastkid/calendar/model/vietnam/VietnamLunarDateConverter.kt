/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.vietnam

import java.util.Calendar
import java.util.GregorianCalendar

class VietnamLunarDateConverter(private val vietnameseCalendar: VietnameseCalendar = VietnameseCalendar()) {

    fun lunarDateToSolar(year: Int, month: Int, day: Int): Calendar {
        val leap = if (vietnameseCalendar.isSolarLeap(year, month)) 1 else 0
        val yearMonthAndDate = vietnameseCalendar.toSolar(day, month, year, leap, 7.0)
        return GregorianCalendar(yearMonthAndDate[0], yearMonthAndDate[1] - 1, yearMonthAndDate[2])
    }

}