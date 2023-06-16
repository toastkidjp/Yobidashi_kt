/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.service.japan

import jp.toastkid.calendar.model.holiday.Holiday

class EquinoxDayCalculator {

    fun calculateVernalEquinoxDay(year: Int): Holiday {
        return Holiday(
            "Vernal equinox day",
            3,
            (20.8431 + 0.242194 * (year - 1980)).toInt() - ((year - 1980) / 4),
        "\uD83C\uDDEF\uD83C\uDDF5"
        )
    }

    fun calculateAutumnalEquinoxDay(year: Int): Holiday {
        return Holiday(
            "Autumnal equinox day",
            9,
            (23.2488 + 0.242194 * (year - 1980)).toInt() - ((year - 1980) / 4),
            "\uD83C\uDDEF\uD83C\uDDF5"
        )
    }

}