package jp.toastkid.yobidashi.libs

import android.content.Context
import android.graphics.Bitmap

import java.io.File

/**
 * Image cache utilities.
 *
 * @author toastkidjp
 */
class ImageCache {

    /**
     * Save bitmap file to cache file.
     *
     * @param context
     * @param bitmap
     *
     * @return
     */
    fun saveBitmap(context: Context, bitmap: Bitmap): File {
        val cacheDir = File(context.cacheDir, CHILD_DIRECTORY)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, System.currentTimeMillis().toString() + FILE_EXTENSION)
        Bitmaps.compress(bitmap, file)
        return file
    }

    companion object {

        private const val CHILD_DIRECTORY = "/cache_images"

        private const val FILE_EXTENSION = ".png"

    }
}
