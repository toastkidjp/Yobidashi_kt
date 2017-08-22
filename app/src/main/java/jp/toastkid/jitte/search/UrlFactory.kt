package jp.toastkid.jitte.search

import android.content.Context
import android.net.Uri

/**
 * @author toastkidjp
 */
internal class UrlFactory {

    fun make(context: Context, category: String, query: String): Uri {
        return Uri.parse(SearchCategory.findByCategory(category).make(context, query))
    }

}
