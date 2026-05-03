/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.loan.model.calculator

import jp.toastkid.loan.model.Factor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrincipalEqualPaymentCalculatorTest {

    private val calculator = PrincipalEqualPaymentCalculator()

    @Test
    fun testCalculate() {
        val factor = Factor(
            amount = 12_000_000L,
            term = 10,
            interestRate = 1.2,
            downPayment = 0,
            managementFee = 10_000,
            renovationReserves = 5_000
        )

        val result = calculator.invoke(factor)

        val totalMonths = 120
        assertEquals(totalMonths, result.paymentSchedule.size)

        val expectedPrincipal = 100_000.0
        result.paymentSchedule.forEach { detail ->
            assertEquals(expectedPrincipal, detail.principal, 0.001)
        }

        val firstInterest = result.paymentSchedule.first().interest
        assertEquals(12_000.0, firstInterest, 0.001)

        assertEquals(127_000L, result.paymentSchedule.first().amount)

        val firstMonthAmount = result.paymentSchedule[0].amount
        val secondMonthAmount = result.paymentSchedule[1].amount
        assertTrue(firstMonthAmount > secondMonthAmount)

        val lastInterest = result.paymentSchedule.last().interest
        assertEquals(100.0, lastInterest, 0.001)
    }

    @Test
    fun testDownPayment() {
        val factor = Factor(
            amount = 10_000_000L,
            term = 10,
            interestRate = 1.0,
            downPayment = 2_000_000,
            managementFee = 0,
            renovationReserves = 0
        )

        val result = calculator.invoke(factor)

        val expectedPrincipal = 8_000_000.0 / 120
        assertEquals(expectedPrincipal, result.paymentSchedule.first().principal, 0.001)
    }

}
