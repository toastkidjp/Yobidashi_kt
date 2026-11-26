package jp.toastkid.web.view.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class SeekControlViewModelTest {

    private lateinit var subject: SeekControlViewModel

    @Before
    fun setUp() {
        subject = SeekControlViewModel()
    }

    @Test
    fun openHourChooser() {
        assertFalse(subject.openingHourChooser().value)

        subject.openHourChooser()

        assertTrue(subject.openingHourChooser().value)

        subject.closeHourChooser()

        assertFalse(subject.openingHourChooser().value)
    }

    @Test
    fun setHour() {
        subject.setHour(1)

        assertEquals(1, subject.hour())
    }

    @Test
    fun openMinuteChooser() {
        assertFalse(subject.openingMinuteChooser().value)

        subject.openMinuteChooser()

        assertTrue(subject.openingMinuteChooser().value)

        subject.closeMinuteChooser()

        assertFalse(subject.openingMinuteChooser().value)
    }

    @Test
    fun setMinute() {
        subject.setMinute(1)

        assertEquals(1, subject.minute())
    }

    @Test
    fun seekEvent() {
        println(subject.seekEvent())
    }

    @Test
    fun openPresetChooser() {
        assertFalse(subject.openingPresetChooser().value)

        subject.openPresetChooser()

        assertTrue(subject.openingPresetChooser().value)

        subject.closePresetChooser()

        assertFalse(subject.openingPresetChooser().value)
    }

    @Test
    fun presetSeekTargets() {
        assertTrue(subject.presetSeekTargets().isNotEmpty())
    }

    @Test
    fun relativeSeekEvents() {
        assertTrue(subject.relativeSeekEvents().isNotEmpty())
    }

    @Test
    fun setCurrentTime() {
        subject.setCurrentTime(GregorianCalendar(2026, Calendar.NOVEMBER, 26, 7, 31, 0))

        assertEquals(8, subject.hour())
        assertEquals(30, subject.minute())
    }

}