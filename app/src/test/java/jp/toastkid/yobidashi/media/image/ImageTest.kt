package jp.toastkid.yobidashi.media.image

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class ImageTest {

    @Test
    fun test() {
        val image = Image("test/abc", "abc")
        assertEquals(0, image.itemCount)
        assertFalse(image.isBucket)
        assertEquals("test/abc", image.makeExcludingId())
        assertEquals("abc", image.makeDisplayName())
    }

    @Test
    fun testBucket() {
        val bucket = Image.makeBucket("bucket/test", "test")
        assertEquals(0, bucket.itemCount)
        assertTrue(bucket.isBucket)
        assertEquals("test", bucket.makeExcludingId())
        assertEquals("bucket/test / 0 images", bucket.makeDisplayName())
    }

}