/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.vietnam

import jp.toastkid.calendar.model.holiday.Holiday
import jp.toastkid.calendar.model.holiday.HolidayCalendar

enum class FixedVietnamHoliday(val month: Int, val date: Int, val title: String) {
    NEW_YEARS_DAY(1, 1, "New year's day"),
    REUNIFICATION_DAY(4, 30, "Reunification Day"),
    MAY_DAY(5, 1, "May day"),
    INDEPENDENCE_DAY(9, 2, "Independence day"),
    ;

    companion object {
        fun find(month: Int): Holiday? {
            return values()
                .firstOrNull { month == it.month }
                ?.let { Holiday(it.title, it.month, it.date, HolidayCalendar.VIETNAM.flag) }
        }
    }
}