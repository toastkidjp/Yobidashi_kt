package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.LocaleWrapper
import java.util.*

/**
 * Web search category.
 *
 * @author toastkidjp
 */
enum class SearchCategory(
        @StringRes val id: Int,
        @DrawableRes val iconId: Int,
        private val host: String,
        private val generator: (l: String, h: String, q: String) -> String = { _, h, q ->  h + q }
    ) {

    GOOGLE(R.string.google,
            R.drawable.ic_google,
            "https://www.google.com/search?q="
    ),
    BING(R.string.bing,
            R.drawable.ic_bing_logo,
            "https://www.bing.com/search?q="
    ),
    DUCKDUCKGO(R.string.search_category_web,
            R.drawable.ic_duckduckgo,
            "https://duckduckgo.com/%s?ia=web",
            { _, h, q -> String.format(h, q)}
    ),
    YANDEX(R.string.search_category_yandex,
            R.drawable.ic_yandex,
            "https://www.yandex.com/search/?text="
    ),
    YAHOO(R.string.search_category_yahoo,
            R.drawable.ic_yahoo,
            "https://search.yahoo.com/search?p="
    ),
    YAHOO_JAPAN(R.string.search_category_yahoo_japan,
            R.drawable.ic_yahoo_japan_logo,
            "https://search.yahoo.co.jp/search?p="
    ),
    IMAGE(R.string.search_category_image,
            R.drawable.ic_image_search,
            "https://www.google.co.jp/search?site=imghp&tbm=isch&q="
    ),
    YOUTUBE(R.string.search_category_youtube,
            R.drawable.ic_video,
            "https://www.youtube.com/results?search_query="
    ),
    WIKIPEDIA(R.string.search_category_wikipedia,
            R.drawable.ic_wikipedia,
            "https://%s.wikipedia.org/w/index.php?search=",
            { l, h, q -> String.format(h, l) + Uri.encode(q) }
    ),
    TWITTER(R.string.search_category_twitter,
            R.drawable.ic_twitter,
            "https://twitter.com/search?src=typd&q="
    ),
    FACEBOOK(R.string.search_category_facebook,
            R.drawable.ic_facebook,
            "https://m.facebook.com/search?query="
    ),
    INSTAGRAM(R.string.search_category_instagram,
            R.drawable.ic_instagram_logo,
            "https://www.instagram.com/explore/tags/"
    ),
    MAP(R.string.search_category_map,
            R.drawable.ic_map,
            "https://www.google.co.jp/maps/place/"
    ),
    APPS(R.string.search_category_apps,
            R.drawable.ic_google_play,
            "https://play.google.com/store/search?q="
    ),
    YELP(R.string.yelp,
            R.drawable.ic_yelp,
            "https://www.yelp.com/search?find_desc="
    ),
    GUTENBERG(R.string.gutenberg,
            R.drawable.ic_local_library_black,
            "http://www.gutenberg.org/ebooks/search/?query="
    ),
    AMAZON(R.string.search_category_shopping,
            R.drawable.ic_amazon,
            "https://www.amazon.co.jp/s/ref=nb_sb_noss?field-keywords=",
            { l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    h + Uri.encode(q)
                } else {
                    "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=" + Uri.encode(q)
                }
            }
    ),
    TECHNICAL_QA(R.string.search_category_technical_qa,
            R.drawable.ic_stackoverflow,
            "https://stackoverflow.com/search?q="
    ),
    TECHNOLOGY(R.string.search_category_technology,
            R.drawable.ic_techcrunch,
            "http://jp.techcrunch.com/search/",
            {l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    h + Uri.encode(q)
                }
                "https://techcrunch.com/search/" + Uri.encode(q)
            }
    ),
    GITHUB(R.string.search_category_github,
            R.drawable.ic_github,
            "https://github.com/search?utf8=%E2%9C%93&type=&q="
    ),
    MVNREPOSITORY(R.string.search_category_mvnrepository,
            R.drawable.ic_mvn,
            "https://mvnrepository.com/search?q="
    );

    /**
     * Make search URL with query.
     *
     * @param context [Context]
     * @param query Query string
     *
     * @return Search result URL
     */
    fun make(context: Context, query: String): String {
        return generate(
                LocaleWrapper.getLocale(context.resources.configuration),
                host,
                query
        )
    }

    /**
     * Generate URL.
     *
     * @param l Locale string
     * @param h Host of search result
     * @param q Search query
     *
     * @return Search result URL
     */
    private fun generate(l: String, h: String, q: String): String = generator(l, h, q)

    companion object {

        /**
         * Find [SearchCategory] by search category.
         *
         * @param category Search category
         * @return [SearchCategory]
         */
        fun findByCategory(category: String): SearchCategory {
            for (f in SearchCategory.values()) {
                if (f.name == category.toUpperCase()) {
                    return f
                }
            }
            return SearchCategory.values()
                    .find { it.name == category.toUpperCase() }
                    .let { if (it == null) { GOOGLE } else { it } }
        }

        /**
         * Find index in values by search category string form.
         *
         * @param category Search category string form
         *
         * @return index
         */
        fun findIndex(category: String): Int =
                values().find { it.name == category.toUpperCase() } ?.ordinal ?: 0

        /**
         * Get default object.
         *
         * @return GOOGLE
         */
        fun getDefault(): SearchCategory = GOOGLE

        /**
         * Get default category name.
         *
         * @return "GOOGLE"
         */
        fun getDefaultCategoryName(): String = getDefault().name
    }
}