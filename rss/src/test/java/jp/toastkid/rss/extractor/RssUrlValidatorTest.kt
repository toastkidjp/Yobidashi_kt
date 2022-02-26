package jp.toastkid.rss.extractor

import io.mockk.every
import io.mockk.mockkObject
import jp.toastkid.lib.Urls
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class RssUrlValidatorTest {

    private lateinit var validator: RssUrlValidator

    @Before
    fun setUp() {
        validator = RssUrlValidator()

        mockkObject(Urls)
        every { Urls.isInvalidUrl(any()) }.returns(false)
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