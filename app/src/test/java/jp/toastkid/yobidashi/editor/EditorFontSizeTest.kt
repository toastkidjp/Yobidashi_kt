package jp.toastkid.yobidashi.editor

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class EditorFontSizeTest {

    @Test
    fun testFindIndex() {
        assertEquals(3, EditorFontSize.findIndex(11))
        assertEquals(4, EditorFontSize.findIndex(12))
        assertEquals(6, EditorFontSize.findIndex(13))
        assertEquals(5, EditorFontSize.findIndex(14))
    }
}