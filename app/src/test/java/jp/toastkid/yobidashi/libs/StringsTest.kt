package jp.toastkid.yobidashi.libs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [Strings]' test cases.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class StringsTest {

    /**
     * Test of [Strings.containsMultiByte].
     */
    @Test
    fun test_containsMultiByte() {
        assertTrue(Strings.containsMultiByte("おはよう"))
        assertTrue(Strings.containsMultiByte("それはB"))
        assertTrue(Strings.containsMultiByte("ＩＴ"))
        assertFalse(Strings.containsMultiByte("abc"))
        assertFalse(Strings.containsMultiByte("123"))
        assertFalse(Strings.containsMultiByte("1b3"))
    }
}
