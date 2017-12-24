package jp.toastkid.yobidashi.search

import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.TextInputs
import java.util.*

/**
 * In site search with Google.
 *
 * @author toastkidjp
 */
object SiteSearch {

    /**
     * Site search URL format.
     */
    private const val FORMAT = "https://www.google.com/search?as_dt=i&as_sitesearch=%s&as_q=%s"

    /**
     * Invoke with [WebView].
     * @param webView
     */
    operator fun invoke(webView: WebView) {
        val context = webView.context
        val textInputLayout = TextInputs.make(context)
        TextInputs.setEmptyAlert(textInputLayout)

        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.title_site_search_by_google)
                .setView(textInputLayout)
                .setCancelable(true)
                .setPositiveButton(R.string.title_search_action) { d, i ->
                    textInputLayout.editText?.text?.let { doAction(webView, it.toString()) }
                    d.dismiss()
                }
                .show()
        textInputLayout.editText?.let { editText ->
            editText.hint = context.getString(R.string.hint_please_input)
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (editText.text.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    return@setOnEditorActionListener false
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doAction(webView, editText.text.toString())
                    dialog.dismiss()
                }
                true
            }
        }

        textInputLayout.requestFocus()
    }

    /**
     * Do search action with [WebView].
     *
     * @param webView
     * @param query
     */
    private inline fun doAction(webView: WebView, query: String) {
        webView.loadUrl(makeUrl(webView.url, query))
    }

    /**
     * Make URL.
     *
     * @param url
     * @param rawQuery
     * @return Search result URL
     */
    private fun makeUrl(url: String, rawQuery: String): String =
            Formatter().format(FORMAT, Uri.parse(url).host, Uri.encode(rawQuery)).toString()
}
