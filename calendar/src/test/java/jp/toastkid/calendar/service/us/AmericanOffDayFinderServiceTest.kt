package jp.toastkid.calendar.service.us

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AmericanOffDayFinderServiceTest {

    private lateinit var subject: AmericanOffDayFinderService

    @Before
    fun setUp() {
        subject = AmericanOffDayFinderService()
    }

    @Test
    fun invoke() {
        assertEquals(2, subject.invoke(2025, 1).size)
    }
}