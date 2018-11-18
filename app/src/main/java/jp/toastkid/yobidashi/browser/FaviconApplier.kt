package jp.toastkid.yobidashi.browser

import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.yobidashi.libs.storage.FilesDir
import java.io.File

/**
 * Favicon applier.
 *
 * @author toastkidjp
 */
class FaviconApplier(context: Context) {

    /**
     * Favicon file directory.
     */
    private val favicons: FilesDir = FilesDir(context, "favicons")

    /**
     * Assign file.
     *
     * @param urlStr URL string
     */
    fun assignFile(urlStr: String): File {
        return favicons.assignNewFile("${urlStr.toUri().host}.png")
    }

    /**
     * Make file path.
     *
     * @param urlStr URL string
     */
    fun makePath(urlStr: String): String {
        return assignFile(urlStr).absolutePath
    }
}