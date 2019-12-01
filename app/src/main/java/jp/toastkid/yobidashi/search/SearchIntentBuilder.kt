package jp.toastkid.yobidashi.search

import android.content.Context

import jp.toastkid.yobidashi.main.MainActivity

/**
 * @author toastkidjp
 */
internal class SearchIntentBuilder(private val context: Context) : SearchIntentLauncher {

    private var category: String = ""

    private var query: String = ""

    private var currentUrl: String? = null

    fun setCategory(category: String): SearchIntentBuilder {
        this.category = category
        return this
    }

    fun setQuery(query: String): SearchIntentBuilder {
        this.query = query
        return this
    }

    fun setCurrentUrl(currentUrl: String?): SearchIntentBuilder {
        this.currentUrl = currentUrl
        return this
    }

    override fun invoke() {
        context.startActivity(
                MainActivity.makeBrowserIntent(
                        context,
                        UrlFactory.make(context, category, query, currentUrl)
                )
        )
    }
}
