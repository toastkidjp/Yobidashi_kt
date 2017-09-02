package jp.toastkid.yobidashi.search

import android.net.Uri
import android.support.v7.app.AlertDialog
import android.webkit.WebView

import java.util.Formatter

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.TextInputs

/**
 * In site search with Google.

 * @author toastkidjp
 */
object SiteSearch {

    /** Site search URL format.  */
    private val FORMAT = "https://www.google.com/search?as_dt=i&as_sitesearch=%s&as_q=%s"

    /**
     * Invoke with [WebView].
     * @param webView
     */
    operator fun invoke(webView: WebView) {
        val context = webView.context
        val textInputLayout = TextInputs.make(context)
        TextInputs.setEmptyAlert(textInputLayout)
        AlertDialog.Builder(context)
                .setTitle(R.string.title_site_search_by_google)
                .setView(textInputLayout)
                .setCancelable(true)
                .setPositiveButton(R.string.title_search_action) { d, i ->
                    webView.loadUrl(makeUrl(webView.url,
                            textInputLayout.editText!!.text.toString()))
                    d.dismiss()
                }
                .show()
    }

    /**
     * Make URL.

     * @param url
     * *
     * @param rawQuery
     * *
     * @return
     */
    private fun makeUrl(url: String, rawQuery: String): String {
        return Formatter().format(FORMAT, Uri.parse(url).host, Uri.encode(rawQuery))
                .toString()
    }
}
