package jp.toastkid.yobidashi.libs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

/**
 * {@link Urls}' test case.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner.class)
public class UrlsTest {

    /**
     * {@link Urls#isInvalidUrl(String)}' test.
     * @throws Exception
     */
    @Test
    public void test_isInvalidUrl() throws Exception {
        assertTrue(Urls.Companion.isInvalidUrl(null));
        assertTrue(Urls.Companion.isInvalidUrl(""));
        assertTrue(Urls.Companion.isInvalidUrl("ftp://tomato"));
        assertFalse(Urls.Companion.isInvalidUrl("http://www.yahoo.com"));
        assertFalse(Urls.Companion.isInvalidUrl("https://www.yahoo.com"));
    }

    /**
     * {@link Urls#isValidUrl(String)}' test.
     * @throws Exception
     */
    @Test
    public void test_isValidUrl() throws Exception {
        assertFalse(Urls.Companion.isValidUrl(null));
        assertFalse(Urls.Companion.isValidUrl(""));
        assertFalse(Urls.Companion.isValidUrl("ftp://tomato"));
        assertTrue(Urls.Companion.isValidUrl("http://www.yahoo.com"));
        assertTrue(Urls.Companion.isValidUrl("https://www.yahoo.com"));
    }

}