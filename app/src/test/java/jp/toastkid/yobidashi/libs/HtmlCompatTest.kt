package jp.toastkid.yobidashi.libs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class HtmlCompatTest {

    @Test
    fun test_fromHtml() {
        assertNull(HtmlCompat.fromHtml(null))
        assertEquals(
                "text",
                HtmlCompat.fromHtml("text")?.toString()
        )
        assertEquals(
                "text",
                HtmlCompat.fromHtml("<font color='red'>text</font>")?.toString()
        )
    }
}