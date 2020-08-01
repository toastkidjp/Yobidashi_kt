package jp.toastkid.yobidashi.editor

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [Quotation]'s test cases.
 *
 * @author toastkidjp
 */
class QuotationTest {

    /**
     * Line separator.
     */
    private val lineSeparator = System.getProperty("line.separator")

    /**
     * Test object.
     */
    private lateinit var quotation: Quotation

    /**
     * Initialize object.
     */
    @Before
    fun setUp() {
        quotation = Quotation()
    }

    /**
     * Test of [Quotation.invoke].
     */
    @Test
    fun testInvoke() {
        assertEquals("> tomato", quotation("tomato"))
        assertEquals(
                "> 1. tomato$lineSeparator> 2. orange$lineSeparator> 3. apple",
                quotation("1. tomato${lineSeparator}2. orange${lineSeparator}3. apple")
        )
    }
}