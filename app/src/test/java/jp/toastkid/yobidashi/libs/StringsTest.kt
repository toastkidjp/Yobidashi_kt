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
     * Test of [MultiByteCharacterInspector.invoke].
     */
    @Test
    fun test_containsMultiByte() {
        assertTrue(multiByteCharacterInspector("おはよう"))
        assertTrue(multiByteCharacterInspector("それはB"))
        assertTrue(multiByteCharacterInspector("ＩＴ"))
        assertFalse(multiByteCharacterInspector("abc"))
        assertFalse(multiByteCharacterInspector("123"))
        assertFalse(multiByteCharacterInspector("1b3"))
    }
}
