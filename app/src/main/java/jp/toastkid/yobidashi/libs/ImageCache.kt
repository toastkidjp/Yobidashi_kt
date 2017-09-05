package jp.toastkid.yobidashi.libs

import android.content.Context
import android.graphics.Bitmap

import java.io.File

/**
 * Image cache utilities.

 * @author toastkidjp
 */
object ImageCache {

    /**
     * Save bitmap file to cache file.
     * @param context
     *
     * @param bitmap
     *
     * @return
     */
    fun saveBitmap(context: Context, bitmap: Bitmap): File {
        val cacheDir = File(context.cacheDir, "/cache_images")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, System.currentTimeMillis().toString() + ".png")
        Bitmaps.compress(bitmap, file)
        return file
    }

}
