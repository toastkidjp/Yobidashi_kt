package jp.toastkid.yobidashi.browser.screenshots

import android.content.Context
import android.graphics.Bitmap

import java.io.File

import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.storage.Storeroom

/**
 * Screen shot.

 * @author toastkidjp
 */
object Screenshot {

    /** Folder of screenshots.  */
    internal val DIR = "screenshots"

    /**
     * Save bitmap file to cache file.
     * @param context
     *
     * @param bitmap
     *
     * @return
     */
    fun save(context: Context, bitmap: Bitmap): File {
        val file = Storeroom(context, DIR).assignNewFile(System.currentTimeMillis().toString() + ".png")
        Bitmaps.compress(bitmap, file)
        return file
    }
}
