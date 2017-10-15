package jp.toastkid.yobidashi.calendar

import java.util.*

/**
 * For using convert [java.util.Calendar]'s month to String.
 *
 * @author toastkidjp
 */
internal object Month {

    /**
     * Return month string form.
     * @param month 1-11
     *
     * @return string form
     */
    operator fun get(month: Int): String = when (month) {
        Calendar.JANUARY -> "January"
        Calendar.FEBRUARY -> "February"
        Calendar.MARCH ->  "March"
        Calendar.APRIL ->  "April"
        Calendar.MAY ->  "May"
        Calendar.JUNE ->  "June"
        Calendar.JULY ->  "July"
        Calendar.AUGUST -> "August"
        Calendar.SEPTEMBER -> "September"
        Calendar.OCTOBER -> "October"
        Calendar.NOVEMBER -> "November"
        Calendar.DECEMBER -> "December"
        else -> ""
    }
}
