package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class BitmapScalingTest {

    private lateinit var bitmapScaling: BitmapScaling

    @Before
    fun setUp() {
        bitmapScaling = BitmapScaling()
    }

    @Test
    fun test_bigger() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8)
        val scaled = bitmapScaling(bitmap, 300.0, 300.0)
        assertEquals(200, scaled.width)
        assertEquals(200, scaled.height)
    }

    @Test
    fun test_smaller() {
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ALPHA_8)
        val scaled = bitmapScaling(bitmap, 200.0, 200.0)
        assertEquals(200, scaled.width)
        assertEquals(200, scaled.height)
    }
}