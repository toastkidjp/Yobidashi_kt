package jp.toastkid.yobidashi.browser

import android.content.Context
import android.net.Uri
import jp.toastkid.yobidashi.libs.storage.FilesDir
import java.io.File

/**
 * Favicon applier.
 *
 * @author toastkidjp
 */
class FaviconApplier(context: Context) {

    private val favicons: FilesDir = FilesDir(context, "favicons")

    fun assignFile(urlstr: String): File {
        return favicons.assignNewFile(Uri.parse(urlstr).host + ".png")
    }

    fun makePath(urlstr: String): String {
        return assignFile(urlstr).absolutePath
    }
}