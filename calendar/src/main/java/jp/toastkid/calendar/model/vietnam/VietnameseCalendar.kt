/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.vietnam

import timber.log.Timber
import kotlin.math.floor
import kotlin.math.sin

class VietnameseCalendar {

    private val julianDay = JulianDay()

    /**
     * Solar longitude in degrees
     * Algorithm from: Astronomical Algorithms, by Jean Meeus, 1998
     * @param jdn - number of days since noon UTC on 1 January 4713 BC
     * @return Normalized longitude to (0, 360)
     */
    private fun calculateSunLongitude(jdn: Double): Double {
        val t = (jdn - 2451545.0) / 36525 // Time in Julian centuries from 2000-01-01 12:00:00 GMT
        val t2 = t * t
        val dr = Math.PI / 180 // degree to radian
        val m =
            357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2 // mean anomaly, degree
        val meanLongitude = 280.46645 + 36000.76983 * t + 0.0003032 * t2 // mean longitude, degree
        val dL = ((1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)) +
                (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        val trueLongitude = meanLongitude + dL // true longitude, degree
        return trueLongitude - 360 * floorToInt(trueLongitude / 360)
    }

    /**
     * Julian day number of the kth new moon after (or before) the New Moon of 1900-01-01 13:51 GMT.
     * Accuracy: 2 minutes
     * Algorithm from: Astronomical Algorithms, by Jean Meeus, 1998
     * @param k
     * @return the Julian date number (number of days since noon UTC on 1 January 4713 BC) of the New Moon
     */
    private fun calculateNewMoon(k: Int): Double {
        val t = k / 1236.85 // Time in Julian centuries from 1900 January 0.5
        val t2 = t * t
        val t3 = t2 * t
        val dr = Math.PI / 180
        val jd1 = (2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3) +
                0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr) // Mean new moon
        val m =
            359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3 // Sun's mean anomaly
        val mpr =
            306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3 // Moon's mean anomaly
        val f =
            21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3 // Moon's argument of latitude
        var c1 =
            (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 = c1 - 0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2 * mpr)
        c1 -= 0.0004 * sin(dr * 3 * mpr)
        c1 =
            c1 + 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 =
            c1 - 0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2 * f + m))
        c1 =
            c1 - 0.0004 * sin(dr * (2 * f - m)) - 0.0006 * sin(dr * (2 * f + mpr))
        c1 += 0.0010 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltaT = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        return jd1 + c1 - deltaT
    }

    private fun floorToInt(d: Double): Int = floor(d).toInt()

    private fun getSunLongitude(dayNumber: Int, timeZone: Double): Double {
        return calculateSunLongitude(dayNumber - 0.5 - timeZone / 24)
    }

    private fun getNewMoonDay(k: Int, timeZone: Double): Int {
        return floorToInt(calculateNewMoon(k) + 0.5 + timeZone / 24)
    }

    private fun getLunarMonth11(year: Int, timeZone: Double): Int {
        val off = julianDay.fromDate(year, 12, 31) - 2415021.076998695
        val k = floorToInt(off / 29.530588853)
        val newMoonDay = getNewMoonDay(k, timeZone)
        val sunLong = floorToInt(getSunLongitude(newMoonDay, timeZone) / 30)
        return if (sunLong >= 9) getNewMoonDay(k - 1, timeZone) else newMoonDay
    }

    private fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        val k = floorToInt(0.5 + (a11 - 2415021.076998695) / 29.530588853)
        var last: Int // Month 11 contains point of sun longutide 3*PI/2 (December solstice)
        var i = 1 // We start with the month following lunar month 11
        var arc = floorToInt(getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone) / 30)
        do {
            last = arc
            i++
            arc = floorToInt(getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone) / 30)
        } while (arc != last && i < 14)
        return i - 1
    }

    /**
     *
     * @param year
     * @param month
     * @param date
     * @param timeZone
     * @return array of [lunarYear, lunarMonth, lunarDay, leapOrNot]
     */
    fun toLunar(year: Int, month: Int, date: Int, timeZone: Double): IntArray {
        val dayNumber = julianDay.fromDate(year, month, date)
        val k = floorToInt((dayNumber - 2415021.076998695) / 29.530588853)
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(year, timeZone)
        var b11 = a11
        val lunarYear = year + (if (a11 >= monthStart) 0 else 1)
        if (a11 >= monthStart) {
            a11 = getLunarMonth11(year - 1, timeZone)
        } else {
            b11 = getLunarMonth11(year + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = floorToInt(((monthStart - a11) / 29).toDouble())
        var lunarLeap = 0
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = 1
                }
            }
        }

        return intArrayOf(
            lunarYear - (if (lunarMonth >= 11 && diff < 4) 1 else 0),
            lunarMonth - (if (lunarMonth > 12) 12 else 0),
            lunarDay,
            lunarLeap
        )
    }

    fun toSolar(
        lunarDay: Int,
        lunarMonth: Int,
        lunarYear: Int,
        lunarLeap: Int,
        timeZone: Double
    ): IntArray {
        val lunarMonthIsUnder11 = lunarMonth < 11
        val a11 = getLunarMonth11(lunarYear - (if (lunarMonthIsUnder11) 1 else 0), timeZone)

        var off = if (lunarMonthIsUnder11) lunarMonth + 1 else lunarMonth - 11

        val b11 = getLunarMonth11(lunarYear + (if (lunarMonthIsUnder11) 0 else 1), timeZone)
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11, timeZone)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) {
                leapMonth += 12
            }

            if (lunarLeap != 0 && lunarMonth != leapMonth) {
                Timber.w("Invalid input! lunarLeap $lunarLeap lunarMonth $lunarMonth leapMonth $leapMonth")
                return intArrayOf(0, 0, 0)
            }

            if (lunarLeap != 0 || off >= leapOff) {
                off += 1
            }
        }

        val monthStart = getNewMoonDay(
            floorToInt(0.5 + (a11 - 2415021.076998695) / 29.530588853) + off,
            timeZone
        )
        return julianDay.toDate(monthStart + lunarDay - 1)
    }

    fun isSolarLeap(year: Int, month: Int): Boolean {
        val isLeap = toLunar(year, month, 1, 7.0)[3]
        return isLeap == 1
    }

}
