package jp.toastkid.web.webview.attachment

import android.webkit.JavascriptInterface

class ScrollEventHandlerAttachment(
    private val showAppBar: () -> Unit,
    private val hideAppBar: () -> Unit
) {

    @JavascriptInterface
    fun onScroll(direction: String) {
        if (direction == "up") {
            showAppBar()
        } else {
            hideAppBar()
        }
    }
}