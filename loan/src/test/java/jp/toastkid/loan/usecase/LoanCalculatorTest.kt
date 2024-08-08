/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.usecase

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import jp.toastkid.loan.model.Factor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoanCalculatorTest {

    @InjectMockKs
    private lateinit var calculator: LoanCalculator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        assertEquals(
            87748,
            calculator.invoke(Factor(25_000_000, 35, 1.0, 1_000_000, 10000, 10000)).monthlyPayment
        )
    }

    @Test
    fun testOverDownPayment() {
        assertEquals(
            20000,
            calculator.invoke(Factor(25_000_000, 35, 1.0, 27_000_000, 10000, 10000)).monthlyPayment
        )
    }

    @Test
    fun testOverIntegerRange() {
        assertEquals(
            70515207,
            calculator.invoke(Factor(25_000_000_000L, 35, 1.0, 27_000_000, 10000, 10000)).monthlyPayment
        )
    }

}