package jp.toastkid.yobidashi.rss.extractor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class RssUrlValidatorTest {

    private lateinit var validator: RssUrlValidator

    @Before
    fun setUp() {
        validator = RssUrlValidator()
    }

    @Test
    fun test() {
        assertFalse(validator(""))
        assertFalse(validator("  "))
        assertFalse(validator("www.yahoo.co.jp"))
        assertFalse(validator("ftp://www.yahoo.co.jp"))
        assertFalse(validator("https://www.yahoo.co.jp"))
        assertTrue(validator("https://github.com/toastkidjp/Yobidashi_kt/commits/master.atom"))
        assertTrue(validator("https://news.yahoo.co.jp/pickup/computer/rss.xml"))
    }

}