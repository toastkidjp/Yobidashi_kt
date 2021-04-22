package jp.toastkid.yobidashi.libs

import android.graphics.Bitmap

import java.io.File

/**
 * Image cache utilities.
 *
 * @author toastkidjp
 */
class ImageCache(
        private val fileProvider: (File?, String) -> File = { parent, name -> File(parent, name) },
        private val bitmapCompressor: BitmapCompressor = BitmapCompressor()
) {

    /**
     * Save bitmap file to cache file.
     *
     * @param parent Parent folder file
     * @param bitmap Bitmap
     *
     * @return temporary file object
     */
    fun saveBitmap(parent: File?, bitmap: Bitmap): File {
        val cacheDir = fileProvider(parent, CHILD_DIRECTORY)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = fileProvider(cacheDir, System.currentTimeMillis().toString() + FILE_EXTENSION)
        bitmapCompressor(bitmap, file)
        return file
    }

    companion object {

        private const val CHILD_DIRECTORY = "/cache_images"

        private const val FILE_EXTENSION = ".png"

    }
}
