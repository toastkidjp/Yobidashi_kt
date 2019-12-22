package jp.toastkid.yobidashi.libs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [MultiByteCharacterInspector]' test cases.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class StringsTest {

    /**
     * Test of [MultiByteCharacterInspector.containsMultiByte].
     */
    @Test
    fun test_containsMultiByte() {
        assertTrue(MultiByteCharacterInspector.containsMultiByte("おはよう"))
        assertTrue(MultiByteCharacterInspector.containsMultiByte("それはB"))
        assertTrue(MultiByteCharacterInspector.containsMultiByte("ＩＴ"))
        assertFalse(MultiByteCharacterInspector.containsMultiByte("abc"))
        assertFalse(MultiByteCharacterInspector.containsMultiByte("123"))
        assertFalse(MultiByteCharacterInspector.containsMultiByte("1b3"))
    }
}
