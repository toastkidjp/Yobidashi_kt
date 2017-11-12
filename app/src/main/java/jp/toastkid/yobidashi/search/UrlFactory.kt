package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri

/**
 * @author toastkidjp
 */
internal object UrlFactory {

    /**
     * Make search [Uri].
     *
     * @param context
     * @param category [SearchCategory]
     * @param query
     */
    fun make(context: Context, category: String, query: String): Uri =
            Uri.parse(SearchCategory.findByCategory(category).make(context, query))

}
