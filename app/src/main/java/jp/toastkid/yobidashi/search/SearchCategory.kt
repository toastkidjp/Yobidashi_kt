package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
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
            private val mHost: String,
            val generator: (l: String, h: String, q: String) -> String = {l, h, q ->  h + q }
    ) {

    GOOGLE(R.string.google,
            R.drawable.googleg_standard_color_18,
            "https://www.google.com/search?q="
    ),
    BING(R.string.bing,
            R.drawable.ic_bing_logo,
            "https://www.bing.com/search?q="
    ),
    DUCKDUCKGO(R.string.search_category_web,
            R.drawable.ic_duckduckgo,
            "https://duckduckgo.com/%s?ia=web",
            {l, h, q -> String.format(h, q)}
    ),
    YANDEX(R.string.search_category_yandex,
            R.drawable.ic_yandex,
            "https://www.yandex.com/search/?text="
    ),
    YAHOO(R.string.search_category_yahoo,
            R.drawable.ic_yahoo,
            "https://search.yahoo.com/search?p="
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
            R.drawable.ic_sns,
            "https://twitter.com/search?src=typd&q="
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
            R.drawable.ic_android_app_green,
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
            R.drawable.ic_shopping,
            "https://www.amazon.co.jp/s/ref=nb_sb_noss?field-keywords=",
            { l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    h + Uri.encode(q)
                }
                "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=" + Uri.encode(q)
            }
    ),
    TECHNICAL_QA(R.string.search_category_technical_qa,
            R.drawable.ic_stackoverflow,
            "https://stackoverflow.com/search?q="
    ),
    TECHNOLOGY(R.string.search_category_technology,
            R.drawable.ic_technology,
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
    ),
    GREP_CODE(R.string.search_category_grep_code,
            R.drawable.ic_grepcode,
            "http://grepcode.com/search/?query="
    ),
    SEARCH_CODE(R.string.search_category_search_code,
            R.drawable.ic_searchcode,
            "https://searchcode.com/?q="
    );

    fun make(context: Context, query: String): String {
        return generate(
                LocaleWrapper.getLocale(context.resources.configuration),
                mHost,
                query
        )
    }

    fun generate(l: String, h: String, q: String): String = generator(l, h, q)

    companion object {

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

        fun findIndex(category: String): Int {
            return values().find { it.name == category.toUpperCase() }
                    .let {
                        if (it == null) {
                            0
                        } else {
                            it.ordinal
                        }
                    }
        }

        fun getDefault(): SearchCategory {
            return GOOGLE
        }

        fun getDefaultCategoryName(): String {
            return getDefault().name
        }
    }
}