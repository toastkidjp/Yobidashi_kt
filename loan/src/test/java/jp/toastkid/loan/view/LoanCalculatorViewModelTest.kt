/*
 * Copyright (c) 2025 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.loan.view

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
        subject.loanAmount().clearText()
        subject.loanAmount().setTextAndPlaceCursorAtEnd("1000000")
    }

    @Test
    fun loanTerm() {
        subject.loanTerm().clearText()
        subject.loanTerm().setTextAndPlaceCursorAtEnd("20")
    }

    @Test
    fun interestRate() {
        subject.interestRate().clearText()
        subject.interestRate().setTextAndPlaceCursorAtEnd("2.0")
    }

    @Test
    fun downPayment() {
        subject.downPayment().clearText()
        subject.downPayment().setTextAndPlaceCursorAtEnd("10000")

        subject.clearDownPayment()
        assertEquals("", subject.downPayment().text)
    }

    @Test
    fun managementFee() {
        subject.managementFee().clearText()
        subject.managementFee().setTextAndPlaceCursorAtEnd("10000")

        val converted = subject.managementFee()

        subject.clearManagementFee()
        assertEquals("", subject.managementFee().text)
    }

    @Test
    fun renovationReserves() {
        subject.renovationReserves().clearText()
        subject.renovationReserves().setTextAndPlaceCursorAtEnd("10000")

        subject.renovationReserves()

        subject.clearRenovationReserves()
        assertTrue(subject.renovationReserves().text.isEmpty())
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