package jp.toastkid.jitte.browser.tab

import android.webkit.WebView

/**
 * History object.

 * @author toastkidjp
 */
internal class History private constructor(private val title: String, private val url: String) {

    fun title(): String {
        return title
    }

    fun url(): String {
        return url
    }

    override fun toString(): String {
        return "History{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}'
    }

    companion object {

        /** Empty object.  */
        val EMPTY = History("", "")

        fun make(title: String, url: String): History {
            return History(title, url)
        }

        fun makeCurrent(webView: WebView): History {
            return History(webView.title, webView.url)
        }
    }
}
