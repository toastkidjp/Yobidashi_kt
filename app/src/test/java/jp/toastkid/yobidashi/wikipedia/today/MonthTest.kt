/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.today

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class MonthTest {

    private val month = Month()

    private val monthNames = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    @Test
    fun test() {
        assertEquals("", month[-1])
        (0..11).forEach {
            assertEquals(monthNames[it], month[it])
        }
        assertEquals("", month[12])
    }
}