package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri

/**
 * @author toastkidjp
 */
class UrlFactory {

    /**
     * Make search [Uri].
     *
     * @param context
     * @param category [SearchCategory]
     * @param query
     * @param currentUrl
     */
    fun make(context: Context, category: String, query: String, currentUrl: String? = null): Uri =
            Uri.parse(SearchCategory.findByCategory(category).make(context, query, currentUrl))

}
