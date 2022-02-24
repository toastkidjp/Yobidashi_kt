package jp.toastkid.yobidashi.wikipedia.today

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class MonthTest {

    private val month = Month()

    private val monthNames = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    @Test
    fun test() {
        assertEquals("", month[-1])
        (0..11).forEach {
            assertEquals(monthNames[it], month[it])
        }
        assertEquals("", month[12])
    }
}