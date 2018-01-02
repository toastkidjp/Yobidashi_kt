package jp.toastkid.yobidashi.browser

import android.os.Build
import android.webkit.WebView

/**
 * For inversion HTML content.
 *
 * @author toastkidjp
 */
object InversionScript {

    /**
     * JavaScript.
     */
    private const val SCRIPT: String = """
(function () {
    var newStyle = document.createElement('style');
    newStyle.type = "text/css";
    var invert = "img { -webkit-filter: invert(100%);} html { -webkit-filter: invert(100%); }"
    if (newStyle.styleSheet) {
      newStyle.styleSheet.cssText = invert;
    } else {
      newStyle.appendChild(document.createTextNode(invert));
    }
    document.getElementsByTagName('head').item(0).appendChild(newStyle);
}());
            """

    /**
     * Invoke action.
     *
     * @param webView [WebView]
     */
    operator fun invoke(webView: WebView) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(SCRIPT, null)
        }
    }
}