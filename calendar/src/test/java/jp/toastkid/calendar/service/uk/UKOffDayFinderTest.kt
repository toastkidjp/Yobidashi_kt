package jp.toastkid.calendar.service.uk

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UKOffDayFinderTest {

    private lateinit var subject: UKOffDayFinder

    @Before
    fun setUp() {
        subject = UKOffDayFinder()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun invoke() {
        assertEquals(1, subject.invoke(2025, 1).size)
    }
}