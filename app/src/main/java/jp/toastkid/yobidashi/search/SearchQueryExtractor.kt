package jp.toastkid.yobidashi.search

import android.net.Uri
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
object SearchQueryExtractor {

    private val commonQueryParameterNames = setOf("q", "query", "text", "word")

    operator fun invoke(url: String?) = invoke(url?.toUri())

    operator fun invoke(uri: Uri?): String? {
        val host = uri?.host ?: return null
        return when {
            host.startsWith("www.google.")
                    or host.startsWith("play.google.")
                    or host.startsWith("www.bing.")
                    or host.endsWith("twitter.com")
                    or host.endsWith("stackoverflow.com")
                    or host.endsWith("github.com")
                    or host.endsWith("mvnrepository.com")
                    or host.endsWith("searchcode.com")
                    or host.startsWith("search.yahoo.com")
                    or host.equals("www.reddit.com")
                    or host.endsWith("medium.com")
                    or host.endsWith("ted.com")
                    or host.endsWith(".slideshare.net")
                    or host.endsWith("cse.google.com")
                    or host.endsWith(".buzzfeed.com")
                    or host.endsWith(".livejournal.com") ->
                uri.getQueryParameter("q")
            host.startsWith("www.amazon.") ->
                uri.getQueryParameter("field-keywords")
            host.startsWith("www.yandex.") ->
                uri.getQueryParameter("text")
            host.startsWith("www.youtube.") ->
                uri.getQueryParameter("search_query")
            host.startsWith("www.flickr.") ->
                uri.getQueryParameter("text")
            host.endsWith(".yelp.com") ->
                uri.getQueryParameter("find_desc")
            host.startsWith("facebook.com") ->
                uri.getQueryParameter("query")
            host.endsWith(".wikipedia.org") ->
                uri.getQueryParameter("search")
            host.endsWith("search.yahoo.co.jp") ->
                uri.getQueryParameter("p")
            host.endsWith("www.baidu.com") ->
                uri.getQueryParameter("wd")
            else -> uri.getQueryParameter(
                    commonQueryParameterNames
                            .find { uri.queryParameterNames.contains(it) } ?: ""
            )
        }
    }
}