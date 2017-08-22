package jp.toastkid.jitte.search

import android.content.Context
import android.support.annotation.ColorInt

import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.intent.CustomTabsFactory
import jp.toastkid.jitte.libs.intent.PendingIntentFactory

/**
 * Search intent launcher.

 * @author toastkidjp
 */
internal class ChromeTabsSearchIntentLauncher(private val context: Context) : SearchIntentLauncher {

    @ColorInt private var backgroundColor: Int = 0

    @ColorInt private var fontColor: Int = 0

    private var category: String = ""

    private var query: String = ""

    fun setBackgroundColor(@ColorInt backgroundColor: Int): ChromeTabsSearchIntentLauncher {
        this.backgroundColor = backgroundColor
        return this
    }

    fun setFontColor(@ColorInt fontColor: Int): ChromeTabsSearchIntentLauncher {
        this.fontColor = fontColor
        return this
    }

    fun setCategory(category: String): ChromeTabsSearchIntentLauncher {
        this.category = category
        return this
    }

    fun setQuery(query: String): ChromeTabsSearchIntentLauncher {
        this.query = query
        return this
    }

    override fun invoke() {
        val intent = CustomTabsFactory.make(
                context, backgroundColor, fontColor, R.drawable.ic_back)
                .addMenuItem(
                        context.getString(R.string.title_search),
                        PendingIntentFactory.makeSearchLauncher(context)
                )
                .addMenuItem(
                        context.getString(R.string.title_settings_color),
                        PendingIntentFactory.makeColorSettingsIntent(context)
                )
                .addMenuItem(
                        context.getString(R.string.title_adding_favorite_search),
                        PendingIntentFactory.favoriteSearchAdding(context, category, query)
                )
                .build()
        intent.launchUrl(context, UrlFactory().make(context, category, query))
    }

}
