package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri

import java.util.Locale

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.LocaleWrapper

/**
 * Web search category.

 * @author toastkidjp
 */
enum class SearchCategory private constructor(val id: Int, val iconId: Int, private val mHost: String, private val mGenerator: Generator = { l, h, q -> h + Uri.encode(q) }) {

    GOOGLE(R.string.google,
            R.drawable.googleg_standard_color_18,
            "https://www.google.com/#q="
    ),
    BING(R.string.bing,
            R.drawable.ic_bing_logo,
            "https://www.bing.com/search?q="
    ),
    WEB(R.string.search_category_web,
            R.drawable.ic_world,
            "https://duckduckgo.com/%s?ia=web",
            { l, h, q -> String.format(h, q) }
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
            R.drawable.ic_android_app,
            "https://play.google.com/store/search?q="
    ),
    YELP(R.string.yelp,
            R.drawable.ic_restaurant,
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
                    return h + Uri.encode(q)
                }
                "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=" + Uri.encode(q)
            }
    ),
    TECHNICAL_QA(R.string.search_category_technical_qa,
            R.drawable.ic_technical_qa,
            "https://stackoverflow.com/search?q="
    ),
    TECHNOLOGY(R.string.search_category_technology,
            R.drawable.ic_technology,
            "http://jp.techcrunch.com/search/",
            { l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    return h + Uri.encode(q)
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
     * URL Generator.
     */
    private interface Generator {
        fun generate(lang: String, host: String, query: String): String
    }

    fun make(context: Context, query: String): String {
        return mGenerator.generate(
                LocaleWrapper.getLocale(context.resources.configuration),
                mHost,
                query
        )
    }

    companion object {

        fun findByCategory(category: String): SearchCategory {
            for (f in SearchCategory.values()) {
                if (f.name == category.toUpperCase()) {
                    return f
                }
            }
            return WEB
        }
    }
}