package jp.toastkid.jitte.calendar

import java.util.Calendar

/**
 * For using convert [java.util.Calendar]'s month to String.

 * @author toastkidjp
 */
internal object Month {

    /**
     * Return month string form.
     * @param month 1-11
     * *
     * @return string form
     */
    operator fun get(month: Int): String {
        when (month) {
            Calendar.JANUARY -> return "January"
            Calendar.FEBRUARY -> return "February"
            Calendar.MARCH -> return "March"
            Calendar.APRIL -> return "April"
            Calendar.MAY -> return "May"
            Calendar.JUNE -> return "June"
            Calendar.JULY -> return "July"
            Calendar.AUGUST -> return "August"
            Calendar.SEPTEMBER -> return "September"
            Calendar.OCTOBER -> return "October"
            Calendar.NOVEMBER -> return "November"
            Calendar.DECEMBER -> return "December"
        }
        return ""
    }
}
