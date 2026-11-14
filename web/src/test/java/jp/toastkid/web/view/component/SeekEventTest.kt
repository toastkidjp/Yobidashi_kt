package jp.toastkid.web.view.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SeekEventTest {

    @Test
    fun toOffsetSeconds() {
        assertEquals(4800, SeekEvent(1, 20).toOffsetSeconds())
    }

}