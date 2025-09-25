package jp.toastkid.calendar.service.uk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UKOffDayFinderTest {

    private lateinit var subject: UKOffDayFinder

    @Before
    fun setUp() {
        subject = UKOffDayFinder()
    }

    @Test
    fun invoke() {
        assertEquals(1, subject.invoke(2025, 1).size)
        assertTrue(subject.invoke(2025, 2).isEmpty())
        assertTrue(subject.invoke(2025, 3).isEmpty())
        assertEquals(2, subject.invoke(2025, 4).size)
        assertEquals(2, subject.invoke(2025, 5).size)
        assertTrue(subject.invoke(2025, 6).isEmpty())
        assertTrue(subject.invoke(2025, 7).isEmpty())
        assertEquals(1, subject.invoke(2025, 8).size)
        assertTrue(subject.invoke(2025, 9).isEmpty())
        assertTrue(subject.invoke(2025, 10).isEmpty())
    }

}
