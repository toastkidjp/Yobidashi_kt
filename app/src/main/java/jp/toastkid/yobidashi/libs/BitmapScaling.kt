package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap

/**
 * @author toastkidjp
 */
object BitmapScaling {

    operator fun invoke(bitmap: Bitmap, samplingWidth: Double, samplingHeight: Double): Bitmap {
        if (samplingWidth > bitmap.width && samplingHeight > bitmap.height) {
            return bitmap
        }

        val resizeScale = if (bitmap.width >= bitmap.height) {
            samplingWidth / bitmap.width
        } else {
            samplingHeight / bitmap.height
        }

        return Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * resizeScale).toInt(),
                (bitmap.height * resizeScale).toInt(),
                true
        )
    }
}