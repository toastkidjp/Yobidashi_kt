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

    private val multiByteCharacterInspector = MultiByteCharacterInspector()

    /**
     * Test of [MultiByteCharacterInspector.containsMultiByte].
     */
    @Test
    fun test_containsMultiByte() {
        assertTrue(multiByteCharacterInspector.containsMultiByte("おはよう"))
        assertTrue(multiByteCharacterInspector.containsMultiByte("それはB"))
        assertTrue(multiByteCharacterInspector.containsMultiByte("ＩＴ"))
        assertFalse(multiByteCharacterInspector.containsMultiByte("abc"))
        assertFalse(multiByteCharacterInspector.containsMultiByte("123"))
        assertFalse(multiByteCharacterInspector.containsMultiByte("1b3"))
    }
}
