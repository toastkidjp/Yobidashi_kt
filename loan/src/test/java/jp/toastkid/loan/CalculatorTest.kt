/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculatorTest {

    private lateinit var calculator: Calculator

    @Before
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun test() {
        assertEquals(
            87748,
            calculator.invoke(25_000_000, 35, 1.0, 1_000_000, 10000, 10000)
        )
    }

    @Test
    fun testOverDownPayment() {
        assertEquals(
            20000,
            calculator.invoke(25_000_000, 35, 1.0, 27_000_000, 10000, 10000)
        )
    }

}