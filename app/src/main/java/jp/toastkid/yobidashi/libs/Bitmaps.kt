package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap
import timber.log.Timber

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Bitmap utilities.

 * @author toastkidjp
 */
object Bitmaps {

    /**
     * Save bitmap to file.

     * @param bitmap
     * *
     * @param file
     */
    fun compress(bitmap: Bitmap, file: File) {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: IOException) {
            Timber.e(e)
            if (fos == null) {
                return
            }

            try {
                fos.close()
            } catch (e1: IOException) {
                Timber.e(e1)
            }

        }

    }
}
