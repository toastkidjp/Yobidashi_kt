/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.today

import java.util.Calendar

/**
 * @author toastkidjp
 */
class Month {

    /**
     * Return month string form.
     * @param month 1-11
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