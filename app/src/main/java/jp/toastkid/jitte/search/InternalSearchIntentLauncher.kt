package jp.toastkid.jitte.search

import android.content.Context

import jp.toastkid.jitte.main.MainActivity

/**
 * @author toastkidjp
 */
internal class InternalSearchIntentLauncher(private val context: Context) : SearchIntentLauncher {

    private var category: String = ""

    private var query: String = ""


    fun setCategory(category: String): InternalSearchIntentLauncher {
        this.category = category
        return this
    }

    fun setQuery(query: String): InternalSearchIntentLauncher {
        this.query = query
        return this
    }

    override fun invoke() {
        context.startActivity(
                MainActivity.makeBrowserIntent(
                        context,
                        UrlFactory().make(context, category, query)
                )
        )
    }
}
