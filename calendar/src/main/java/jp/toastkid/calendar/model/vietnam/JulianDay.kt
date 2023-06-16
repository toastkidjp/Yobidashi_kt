/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.vietnam

internal class JulianDay {

    /**
     *
     * @param year
     * @param month
     * @param date
     * @return the number of days since 1 January 4713 BC (Julian calendar)
     */
    fun fromDate(year: Int, month: Int, date: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jd = date + (153 * m + 2) / 5 + DAYS_OF_YEAR * y + y / 4 - y / 100 + y / 400 - 32045
        return if (jd <= LAST_DAY_OF_USING_JULIAN_CALENDAR)
            date + (153 * m + 2) / 5 + DAYS_OF_YEAR * y + y / 4 - 32083
        else
            jd
    }

    /**
     * Section: Is there a formula for calculating the Julian day number?
     *
     * @param julianDay - the number of days since 1 January 4713 BC (Julian calendar)
     * @see <a href="http://www.tondering.dk/claus/calendar.html">Calendar</a>
     * @return [year, month, day]
     */
    fun toDate(julianDay: Int): IntArray {
        // After 5/10/1582, Gregorian calendar
        val after1582Oct5 = julianDay > LAST_DAY_OF_USING_JULIAN_CALENDAR

        val a = if (after1582Oct5) julianDay + 32044 else 0
        val b = if (after1582Oct5) (4 * a + 3) / 146097 else 0
        val c = if (after1582Oct5) a - b * 146097 / 4 else julianDay + 32082
        val d = (4 * c + 3) / 1461
        val e = c - 1461 * d / 4
        val m = (5 * e + 2) / 153

        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + m / 10
        return intArrayOf(year, month, day)
    }

}

private const val LAST_DAY_OF_USING_JULIAN_CALENDAR = 2299160

private const val DAYS_OF_YEAR = 365
