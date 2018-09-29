package jp.toastkid.yobidashi.editor

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [Quotation]'s test cases.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class QuotationTest {

    /**
     * Line separator.
     */
    private val lineSeparator = System.getProperty("line.separator")

    /**
     * Test of [Quotation.invoke].
     */
    @Test
    fun testInvoke() {
        assertEquals("> tomato", Quotation("tomato"))
        assertEquals(
                "> 1. tomato$lineSeparator> 2. orange$lineSeparator> 3. apple",
                Quotation("1. tomato${lineSeparator}2. orange${lineSeparator}3. apple")
        )
    }
}