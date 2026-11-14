package jp.toastkid.web.view.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.GregorianCalendar

class PresetSeekTargetTest {

    @Test
    fun test() {
        val preset = PresetSeekTarget.fromText("12:00")
        val offset = preset.calculateOffset(GregorianCalendar(2026, 10, 13, 13, 12, 0))
        assertEquals("SeekEvent(hour=1, minute=11)", offset.toString())
    }

    @Test
    fun futureCase() {
        val preset = PresetSeekTarget.fromText("12:00")
        val offset = preset.calculateOffset(GregorianCalendar(2026, 10, 13, 11, 12, 0))

        assertNull(offset)
    }

}