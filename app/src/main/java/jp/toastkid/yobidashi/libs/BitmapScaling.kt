package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap

/**
 * Scaling [Bitmap] with specified sampling size.
 *
 * @author toastkidjp
 */
object BitmapScaling {

    /**
     * Invoke scaling.
     *
     * @param bitmap [Bitmap]
     * @param samplingWidth specify new bitmap width
     * @param samplingHeight specify new bitmap height
     */
    operator fun invoke(bitmap: Bitmap, samplingWidth: Double, samplingHeight: Double): Bitmap {
        if (samplingWidth > bitmap.width && samplingHeight > bitmap.height) {
            return bitmap
        }

        val resizeScale = calculateResizeScale(bitmap, samplingWidth, samplingHeight)

        return Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * resizeScale).toInt(),
                (bitmap.height * resizeScale).toInt(),
                true
        )
    }

    /**
     * Calculate resizing scale.
     *
     * @param bitmap [Bitmap]
     * @param samplingWidth specify new bitmap width
     * @param samplingHeight specify new bitmap height
     */
    private fun calculateResizeScale(bitmap: Bitmap, samplingWidth: Double, samplingHeight: Double) =
            if (bitmap.width >= bitmap.height) samplingWidth / bitmap.width
            else samplingHeight / bitmap.height
}