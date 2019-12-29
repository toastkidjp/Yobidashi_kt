package jp.toastkid.yobidashi.libs

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
     * @param parent Parent folder file
     * @param bitmap Bitmap
     *
     * @return
     */
    fun saveBitmap(parent: File?, bitmap: Bitmap): File {
        val cacheDir = File(parent, CHILD_DIRECTORY)
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
