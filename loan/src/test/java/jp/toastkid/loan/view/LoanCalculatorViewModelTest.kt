/*
 * Copyright (c) 2025 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.loan.view

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoanCalculatorViewModelTest {

    private lateinit var subject: LoanCalculatorViewModel

    @Before
    fun setUp() {
        subject = LoanCalculatorViewModel()
    }

    @Test
    fun result() {
        assertTrue(subject.result().isEmpty())
    }

    @Test
    fun loanAmount() {
        val newValue = TextFieldValue("1000000", TextRange(1))
        subject.updateLoanAmount(newValue)

        val converted = subject.loanAmount()
        assertEquals("1,000,000", converted.text)
        assertEquals(1, converted.selection.start)
    }

    @Test
    fun loanTerm() {
        val newValue = TextFieldValue("20", TextRange(1))
        subject.updateLoanTerm(newValue)

        val converted = subject.loanTerm()
        assertEquals("20", converted.text)
        assertEquals(1, converted.selection.start)
    }

    @Test
    fun interestRate() {
        val newValue = TextFieldValue("2.0", TextRange(1))
        subject.updateInterestRate(newValue)

        val converted = subject.interestRate()
        assertEquals("2.0", converted.text)
        assertEquals(1, converted.selection.start)
    }

    @Test
    fun downPayment() {
        val newValue = TextFieldValue("10000", TextRange(1))
        subject.updateDownPayment(newValue)

        val converted = subject.downPayment()
        assertEquals("10,000", converted.text)
        assertEquals(1, converted.selection.start)

        subject.clearDownPayment()
        assertEquals("0", subject.downPayment().text)
    }

    @Test
    fun managementFee() {
        val newValue = TextFieldValue("10000", TextRange(1))
        subject.updateManagementFee(newValue)

        val converted = subject.managementFee()
        assertEquals("10,000", converted.text)
        assertEquals(1, converted.selection.start)

        subject.clearManagementFee()
        assertEquals("0", subject.managementFee().text)
    }

    @Test
    fun renovationReserves() {
        val newValue = TextFieldValue("10000", TextRange(1))
        subject.updateRenovationReserves(newValue)

        val converted = subject.renovationReserves()
        assertEquals("10,000", converted.text)
        assertEquals(1, converted.selection.start)

        subject.clearRenovationReserves()
        assertEquals("0", subject.renovationReserves().text)
    }

    @Test
    fun scheduleState() {
        assertTrue(subject.scheduleState().isEmpty())
    }

    @Test
    fun scrollState() {
        assertEquals(0, subject.scrollState().firstVisibleItemIndex)
    }

    @Test
    fun roundToIntSafely() {
        assertEquals("0", subject.roundToIntSafely(Double.NaN))
        assertEquals("0", subject.roundToIntSafely(0.1))
    }

    @Test
    fun launch() {
        // TODO
    }

}