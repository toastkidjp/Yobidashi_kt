/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.model.uk

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import java.util.Calendar

enum class MoveableUKHoliday(private val month: Int, val week: Int, val dayOfWeek: Int = Calendar.MONDAY, val title: String) {
    EARLY_MAY_BANK_HOLIDAY(5, -1, title = "Early May Bank Holiday"),
    SPRING_BANK_HOLIDAY(5, -1, title = "Spring Bank Holiday"),
    SUMMER_BANK_HOLIDAY(8, -1, title = "Summer Bank Holiday")
    ;

    companion object {

        fun find(month: Int): List<Holiday> = emptyList()

    }
}