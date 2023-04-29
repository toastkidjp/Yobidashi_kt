/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.model.japan

enum class MoveableJapaneseHoliday(val title: String, private val month: Int, val week: Int) {

    COMING_OF_AGE_DAY("Coming of age day", 1, 2),
    MARINE_DAY("Marine day", 7, 3),
    RESPECT_FOR_THE_AGED_DAY("Respect for the aged day", 9, 3),
    SPORTS_DAY("Sports day", 10, 2)
    ;

    companion object {
        private val months = values().map { it.month }.distinct()

        fun isTargetMonth(month: Int): Boolean {
            return months.contains(month)
        }

        fun find(month: Int): MoveableJapaneseHoliday? {
            return values().firstOrNull { it.month == month }
        }

    }

}