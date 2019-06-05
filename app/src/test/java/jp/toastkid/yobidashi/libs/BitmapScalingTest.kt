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
    fun test_invoke() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8)
        val scaled = BitmapScaling(bitmap, 300.0, 300.0)
        assertEquals(300, scaled.width)
        assertEquals(300, scaled.height)
    }
}