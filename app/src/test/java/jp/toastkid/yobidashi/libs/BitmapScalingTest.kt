package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class BitmapScalingTest {

    @Test
    fun test_bigger() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8)
        val scaled = BitmapScaling(bitmap, 300.0, 300.0)
        assertEquals(200, scaled.width)
        assertEquals(200, scaled.height)
    }

    @Test
    fun test_smaller() {
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ALPHA_8)
        val scaled = BitmapScaling(bitmap, 200.0, 200.0)
        assertEquals(200, scaled.width)
        assertEquals(200, scaled.height)
    }
}