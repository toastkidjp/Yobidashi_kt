package jp.toastkid.yobidashi.wikipedia.today

import org.junit.Assert.*
import org.junit.Test

/**
 * @author toastkidjp
 */
class MonthTest {

    private val month = Month()

    @Test
    fun test() {
        assertEquals("", month[-1])
        assertEquals("January", month[0])
        assertEquals("December", month[11])
        assertEquals("", month[12])
    }
}