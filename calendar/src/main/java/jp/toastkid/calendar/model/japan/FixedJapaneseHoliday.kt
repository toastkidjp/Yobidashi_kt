/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.japan

import jp.toastkid.calendar.model.holiday.Holiday

enum class FixedJapaneseHoliday(val month: Int, val date: Int, val japaneseTitle: String) {
    NATIONAL_FOUNDATION_DAY(2, 11, "建国記念の日"),
    EMPERORS_BIRTHDAY(2, 23, "天皇誕生日"),
    SHOWA_DAY(4,29, "昭和の日"),
    CONSTITUTION_MEMORIAL_DAY(5, 3, "憲法記念日"),
    GREENERY_DAY(5, 4, "みどりの日"),
    CHILDREN_S_DAY(5, 5, "こどもの日"),
    MOUNTAIN_DAY(8, 11, "山の日"),
    CULTURE_DAY(11, 3, "文化の日"),
    LABOR_THANKSGIVING_DAY(11, 23, "勤労感謝の日"),
    ;

    companion object {
        fun find(year: Int, month: Int): List<Holiday> {
            return values().filter { it.month == month }
                .mapNotNull {
                    val day = calculateDate(year, it) ?: return@mapNotNull null
                    Holiday(
                        it.japaneseTitle,
                        it.month,
                        day,
                        "\uD83C\uDDEF\uD83C\uDDF5"
                    )
                }
        }

        private inline fun calculateDate(year: Int, it: FixedJapaneseHoliday): Int? {
            if (it != MOUNTAIN_DAY) {
                return it.date
            }

            if (year <= 2015) {
                return null
            }

            return when (year) {
                2020 -> 10
                2021 -> 8
                else -> it.date
            }
        }

    }
}