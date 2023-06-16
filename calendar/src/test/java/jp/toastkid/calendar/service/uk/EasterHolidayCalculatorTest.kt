/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.service.uk

import org.junit.Assert.assertEquals
import org.junit.Test

class EasterHolidayCalculatorTest {

    @Test
    fun test() {
        val calculator = EasterHolidayCalculator()
        val holidays = (2022..2024).map { calculator.invoke(it, 4) }
        // 2022
        assertEquals(4, holidays[0][0].month)
        assertEquals(15, holidays[0][0].day)
        assertEquals(4, holidays[0][1].month)
        assertEquals(18, holidays[0][1].day)
        // 2023
        assertEquals(4, holidays[1][0].month)
        assertEquals(7, holidays[1][0].day)
        assertEquals(4, holidays[1][1].month)
        assertEquals(10, holidays[1][1].day)
        // 2024
        assertEquals(4, holidays[2][0].month)
        assertEquals(1, holidays[2][0].day)

        val march = calculator.invoke(2024, 3)
        assertEquals(3, march[0].month)
        assertEquals(29, march[0].day)
    }

}