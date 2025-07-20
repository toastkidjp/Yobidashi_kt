package jp.toastkid.world.presentation

import android.text.format.DateFormat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class WorldTimeViewModelTest {

    private lateinit var subject: WorldTimeViewModel

    @Before
    fun setUp() {
        mockkStatic(DateFormat::class)
        every { DateFormat.format(any(), any<Calendar>()) } returns ""

        subject = WorldTimeViewModel()
        subject.start()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun listState() {
        assertEquals(0, subject.listState().firstVisibleItemIndex)
    }

    @Test
    fun pickupTimeZone() {
        assertTrue(subject.pickupTimeZone().isNotEmpty())
    }

    @Test
    fun openingChooser() {
        assertFalse(subject.openingChooser())

        subject.openChooser()

        assertTrue(subject.openingChooser())

        subject.closeChooser()

        assertFalse(subject.openingChooser())
    }

    @Test
    fun choose() {
        subject.openChooser()

        subject.choose("Asia/Seoul")

        assertFalse(subject.openingChooser())
        println(subject.currentTimezoneLabel())
    }

    @Test
    fun openingHourChooser() {
        assertFalse(subject.openingHourChooser())

        subject.openHourChooser()

        assertTrue(subject.openingHourChooser())

        subject.closeHourChooser()

        assertFalse(subject.openingHourChooser())
    }

    @Test
    fun chooseHour() {
        subject.openHourChooser()

        subject.chooseHour(13)

        assertEquals("13", subject.currentHour())
        assertFalse(subject.openingHourChooser())
    }

    @Test
    fun openingMinuteChooser() {
        assertFalse(subject.openingMinuteChooser())

        subject.openMinuteChooser()

        assertTrue(subject.openingMinuteChooser())

        subject.closeMinuteChooser()

        assertFalse(subject.openingMinuteChooser())
    }

    @Test
    fun chooseMinute() {
        subject.openMinuteChooser()

        subject.chooseMinute(13)

        assertEquals("13", subject.currentMinute())
        assertFalse(subject.openingMinuteChooser())
    }

    @Test
    fun start() {
        assertTrue(subject.items().isNotEmpty())
    }

    @Test
    fun label() {
        assertEquals("\uD83C\uDDEF\uD83C\uDDF5 Asia/Tokyo", subject.label("Asia/Tokyo"))
    }
}