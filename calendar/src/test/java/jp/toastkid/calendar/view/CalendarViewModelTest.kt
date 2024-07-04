/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.view

import io.mockk.unmockkAll
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalendarViewModelTest {

    private lateinit var subject: CalendarViewModel

    @org.junit.Before
    fun setUp() {
        subject = CalendarViewModel()
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun openDateArticle() {
    }

    @org.junit.Test
    fun isToday() {
    }

    @org.junit.Test
    fun makeMonth() {
    }

    @org.junit.Test
    fun getDayOfWeekLabel() {
        val week = subject.week()
        assertEquals(
            week.size,
            week.map { subject.getDayOfWeekLabel(it) }.filter { it.length != 0 }.size
        )
        assertTrue(subject.getDayOfWeekLabel(22).isEmpty())
    }

    @org.junit.Test
    fun calculateHolidays() {
    }

    @org.junit.Test
    fun year() {
    }

    @org.junit.Test
    fun currentYearLabel() {
    }

    @org.junit.Test
    fun currentMonthLabel() {
    }

    @org.junit.Test
    fun week() {
    }

    @org.junit.Test
    fun calculateInitialPage() {
        val calculateInitialPage = subject.calculateInitialPage()
        assertEquals(subject.toPage(Calendar.getInstance()), calculateInitialPage)
    }

    @org.junit.Test
    fun fromPage() {
        val calendar = subject.fromPage(24281)
        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, calendar.get(Calendar.MONTH))
    }

    @Test
    fun toPage() {
        val calendar = subject.fromPage(24281)
        calendar.add(Calendar.MONTH, 1)

        val toPage = subject.toPage(calendar)
        assertEquals(24282, toPage)

        val reConverted = subject.fromPage(toPage)
        assertEquals(2024, reConverted.get(Calendar.YEAR))
        assertEquals(Calendar.JULY, reConverted.get(Calendar.MONTH))
    }

}