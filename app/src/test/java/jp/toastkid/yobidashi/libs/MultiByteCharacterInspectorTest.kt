package jp.toastkid.yobidashi.libs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [MultiByteCharacterInspector]' test cases.
 *
 * @author toastkidjp
 */
class MultiByteCharacterInspectorTest {

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
