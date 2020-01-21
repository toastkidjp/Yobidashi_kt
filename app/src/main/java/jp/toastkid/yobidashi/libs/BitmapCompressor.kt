package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap
import timber.log.Timber

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * This class is used for compressing bitmap to file.
 *
 * @author toastkidjp
 */
class BitmapCompressor {

    /**
     * Save bitmap to PNG file.
     *
     * @param bitmap
     * @param file
     */
    operator fun invoke(bitmap: Bitmap, file: File) {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: IOException) {
            Timber.e(e)
            try {
                fos?.close()
            } catch (e1: IOException) {
                Timber.e(e1)
            }
        }

    }
}
