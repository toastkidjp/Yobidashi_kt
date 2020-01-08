package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri

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
    operator fun invoke(
            context: Context,
            category: String,
            query: String,
            currentUrl: String? = null
    ): Uri = SearchCategory.findByCategory(category).make(context, query, currentUrl).toUri()

}
