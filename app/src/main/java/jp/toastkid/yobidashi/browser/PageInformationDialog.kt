package jp.toastkid.yobidashi.browser

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AlertDialog
import android.webkit.WebView

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Method object of displaying page information dialog.
 *
 * @param webView
 * @author toastkidjp
 */
internal class PageInformationDialog(private val webView: WebView) {

    /**
     * Context.
     */
    private val context: Context = webView.context

    /**
     * Show dialog.
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
                .setTitle(R.string.title_menu_page_information)
                .setMessage(makeMessage())
                .setCancelable(true)
                .setNeutralButton("Clip URL") { d, _ -> clipUrl(d) }
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
        if (webView.favicon != null) {
            builder.setIcon(BitmapDrawable(context.resources, webView.favicon))
        }
        builder.show()
    }

    /**
     * Copy URL to Clipboard.
     *
     * @param d
     */
    private fun clipUrl(d: DialogInterface) {
        Clipboard.clip(context, webView.url)
        Toaster.snackShort(
                webView,
                "It has copied URL to clipboard.${lineSeparator}${webView.url}",
                PreferenceApplier(context).colorPair()
        )
        d.dismiss()
    }

    /**
     * Make message.
     */
    private fun makeMessage(): String =
            "Title: ${webView.title}${lineSeparator}URL: ${webView.url}${lineSeparator}"

    companion object {

        /**
         * Line separator.
         */
        private val lineSeparator = System.getProperty("line.separator")

    }
}
