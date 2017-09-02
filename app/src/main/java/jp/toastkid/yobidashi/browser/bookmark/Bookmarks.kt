package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import jp.toastkid.yobidashi.libs.storage.Storeroom

/**
 * @author toastkidjp
 */
object Bookmarks {

    fun makeFaviconUrl(context: Context, url: String): String {
        return Storeroom(context, "favicons").assignNewFile(Uri.parse(url).host + ".png").absolutePath
    }
}