package jp.toastkid.yobidashi.editor

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class QuotationTest {

    private val lineSeparator = System.getProperty("line.separator")

    @Test
    operator fun invoke() {
        assertEquals("> tomato", Quotation("tomato"))
        assertEquals(
                "> 1. tomato$lineSeparator> 2. orange$lineSeparator> 3. apple",
                Quotation("1. tomato${lineSeparator}2. orange${lineSeparator}3. apple")
        )
    }
}