package jp.toastkid.lib.lifecycle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class EventTest {

    @Test
    fun testPeekContent() {
        val event = Event("test")
        repeat(3) { assertEquals("test", event.peekContent()) }
    }

    @Test
    fun testHandled() {
        val event = Event("test")
        assertFalse(event.hasBeenHandled)
        assertEquals("test", event.getContentIfNotHandled())

        assertTrue(event.hasBeenHandled)
        assertNull(event.getContentIfNotHandled())
    }

}