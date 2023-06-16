/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.us

import jp.toastkid.calendar.model.holiday.Holiday

enum class FixedAmericanHoliday(val month: Int, val date: Int, val title: String) {
    NEW_YEAR(1, 1, "New year's day"),
    JUNE_TEENS(6, 19, "June Teens"),
    INDEPENDENCE_DAY(7, 4, "Independence Day"),
    VETERANS_DAY(11, 11, "Veterans Day"),
    CHRISTMAS(12, 25, "Christmas");

    companion object {
        fun find(year: Int, month: Int): Holiday? {
            if (year < 2021 && month == 6) {
                return null
            }

            return values()
                .firstOrNull { month == it.month }
                ?.let { Holiday(it.title, it.month, it.date) }
        }
    }
}
