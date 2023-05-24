/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.japan

class EquinoxDayCalculator {

    fun calculateVernalEquinoxDay(year: Int): Int {
        return (20.8431 + 0.242194 * (year - 1980)).toInt() - ((year - 1980) / 4)
    }

    fun calculateAutumnalEquinoxDay(year: Int): Int {
        return (23.2488 + 0.242194 * (year - 1980)).toInt() - ((year - 1980) / 4)
    }

}