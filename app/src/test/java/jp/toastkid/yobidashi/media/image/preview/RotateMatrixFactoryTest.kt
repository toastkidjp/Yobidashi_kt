package jp.toastkid.yobidashi.media.image.preview

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class RotateMatrixFactoryTest {

    private val expected = floatArrayOf(
            0.9998477f,
            -0.017452406f,
            0.8802348f,
            0.017452406f,
            0.9998477f,
            -0.8650058f,
            0.0f,
            0.0f,
            1.0f
    )

    @Test
    fun test() {
        val matrix = RotateMatrixFactory().invoke(1f, 100f, 100f)

        val floats = FloatArray(9)
        matrix.getValues(floats)

        assertTrue(expected.contentEquals(floats))
    }
}