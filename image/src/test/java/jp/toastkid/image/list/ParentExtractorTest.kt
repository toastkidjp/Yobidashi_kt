package jp.toastkid.image.list

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class ParentExtractorTest {

    @Test
    fun test() {
        val parentExtractor = ParentExtractor()
        assertNull(parentExtractor.invoke(null))
        assertTrue(parentExtractor("")!!.isEmpty())
        assertTrue(parentExtractor("/")!!.isEmpty())
        assertEquals(
                "/storage/emulated/0/Pictures/test",
                parentExtractor("/storage/emulated/0/Pictures/test/20180830_133608.jpg")
        )
        assertEquals("20180830_133608.jpg", parentExtractor("20180830_133608.jpg"))
    }
}