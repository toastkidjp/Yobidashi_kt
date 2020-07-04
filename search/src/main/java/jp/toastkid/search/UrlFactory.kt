package jp.toastkid.search

import android.net.Uri
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
class UrlFactory {

    /**
     * Make search [Uri].
     *
     * @param category [SearchCategory]
     * @param query
     * @param currentUrl
     */
    operator fun invoke(
            category: String,
            query: String,
            currentUrl: String? = null
    ): Uri = SearchCategory.findByCategory(category).make(query, currentUrl).toUri()

}
