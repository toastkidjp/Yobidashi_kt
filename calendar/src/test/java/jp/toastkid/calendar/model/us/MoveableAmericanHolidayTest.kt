/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.us

import org.junit.Assert.assertTrue
import org.junit.Test

class MoveableAmericanHolidayTest {

    @Test
    fun test() {
        assertTrue(MoveableAmericanHoliday.isHoliday(2024, 1, 15))
        assertTrue(MoveableAmericanHoliday.isHoliday(2023, 2, 20))
        assertTrue(MoveableAmericanHoliday.isHoliday(2024, 2, 19))
        assertTrue(MoveableAmericanHoliday.isHoliday(2020, 5, 25))
        assertTrue(MoveableAmericanHoliday.isHoliday(2021, 5, 31))
        assertTrue(MoveableAmericanHoliday.isHoliday(2022, 5, 30))
        assertTrue(MoveableAmericanHoliday.isHoliday(2023, 5, 29))
        assertTrue(MoveableAmericanHoliday.isHoliday(2024, 5, 27))
        assertTrue(MoveableAmericanHoliday.isHoliday(2023, 9, 4))
        assertTrue(MoveableAmericanHoliday.isHoliday(2020, 11, 26))
        assertTrue(MoveableAmericanHoliday.isHoliday(2023, 11, 23))
        assertTrue(MoveableAmericanHoliday.isHoliday(2024, 11, 28))
    }

}