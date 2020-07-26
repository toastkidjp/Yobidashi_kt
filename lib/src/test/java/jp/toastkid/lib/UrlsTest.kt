package jp.toastkid.lib

import jp.toastkid.lib.Urls
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [Urls]' test case.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class UrlsTest {

    /**
     * [Urls.isInvalidUrl]' test.
     * @throws Exception
     */
    @Test
    fun test_isInvalidUrl() {
        assertTrue(Urls.isInvalidUrl(""))
        assertTrue(Urls.isInvalidUrl("ftp://tomato"))
        assertFalse(Urls.isInvalidUrl("http://www.yahoo.com"))
        assertFalse(Urls.isInvalidUrl("https://www.yahoo.com"))
    }

    /**
     * [Urls.isValidUrl]' test.
     * @throws Exception
     */
    @Test
    fun test_isValidUrl() {
        assertFalse(Urls.isValidUrl(""))
        assertFalse(Urls.isValidUrl("ftp://tomato"))
        assertTrue(Urls.isValidUrl("http://www.yahoo.com"))
        assertTrue(Urls.isValidUrl("https://www.yahoo.com"))
    }

}