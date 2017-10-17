package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import jp.toastkid.yobidashi.libs.storage.FilesDir

/**
 * @author toastkidjp
 */
object Bookmarks {

    fun makeFaviconUrl(context: Context, url: String): String {
        return FilesDir(context, "favicons").assignNewFile(Uri.parse(url).host + ".png").absolutePath
    }
}