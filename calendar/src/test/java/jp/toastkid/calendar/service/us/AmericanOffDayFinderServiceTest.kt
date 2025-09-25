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
        assertEquals(1, subject.invoke(2025, 2).size)
        assertEquals(0, subject.invoke(2025, 3).size)
        assertEquals(0, subject.invoke(2025, 4).size)
    }
}