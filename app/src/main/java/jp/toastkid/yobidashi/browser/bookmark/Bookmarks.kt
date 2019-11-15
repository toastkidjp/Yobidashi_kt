package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.yobidashi.libs.storage.FilesDir

/**
 * @author toastkidjp
 */
object Bookmarks {

    fun makeFaviconUrl(context: Context, url: String): String {
        val host = url.toUri().host ?: url
        return FilesDir(context, "favicons").assignNewFile("$host.png").absolutePath
    }
}