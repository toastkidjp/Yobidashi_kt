package jp.toastkid.search

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import jp.toastkid.search.SearchCategory

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
