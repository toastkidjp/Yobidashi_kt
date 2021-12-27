/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorTest {

    @Test
    fun test() {
        assertEquals(
            87748,
            Calculator().invoke(25_000_000, 35, 1.0, 1_000_000, 10000, 10000)
        )
    }

    @Test
    fun testOverDownPayment() {
        assertEquals(
            20000,
            Calculator().invoke(25_000_000, 35, 1.0, 27_000_000, 10000, 10000)
        )
    }

}