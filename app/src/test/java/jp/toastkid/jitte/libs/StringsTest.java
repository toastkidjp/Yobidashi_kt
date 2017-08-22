package jp.toastkid.jitte.libs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link Strings}' test cases.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner.class)
public class StringsTest {

    /**
     * Test of {@link Strings#containsMultiByte(String)}.
     */
    @Test
    public void test_containsMultiByte() {
        assertTrue(Strings.Companion.containsMultiByte("おはよう"));
        assertTrue(Strings.Companion.containsMultiByte("それはB"));
        assertTrue(Strings.Companion.containsMultiByte("ＩＴ"));
        assertFalse(Strings.Companion.containsMultiByte("abc"));
        assertFalse(Strings.Companion.containsMultiByte("123"));
        assertFalse(Strings.Companion.containsMultiByte("1b3"));
    }
}
