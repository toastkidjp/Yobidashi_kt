package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AlertDialog
import android.webkit.WebView

import jp.toastkid.yobidashi.R

/**
 * Method object of displaying page information dialog.

 * @author toastkidjp
 */
internal class PageInformationDialog(private val webView: WebView) {

    private val context: Context

    init {
        this.context = webView.context
    }

    fun show() {
        val builder = AlertDialog.Builder(context)
                .setTitle(R.string.title_menu_page_information)
                .setMessage("Title: " + webView.title
                        + System.getProperty("line.separator")
                        + "URL: " + webView.url
                        + System.getProperty("line.separator")
                )
                .setCancelable(true)
                .setPositiveButton(R.string.close) { d, i -> d.dismiss() }
        if (webView.favicon != null) {
            builder.setIcon(BitmapDrawable(context.resources, webView.favicon))
        }
        builder.show()
    }
}
