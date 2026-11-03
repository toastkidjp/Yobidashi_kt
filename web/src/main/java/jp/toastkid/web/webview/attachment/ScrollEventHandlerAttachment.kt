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

    companion object {

        private const val script = """
    (function() {
        if (window.__scrollObserverInstalled) return "already installed";
        window.__scrollObserverInstalled = true;

        function attachScrollListener() {
            const container = document.querySelector('section[data-chat-scroll]');
            if (!container) return false;
            if (container.dataset.scrollHooked === "true") return true;

            container.dataset.scrollHooked = "true";
            let lastScrollTop = 0;

            container.addEventListener('scroll', function() {
                const st = container.scrollTop;
                if (st < lastScrollTop) {
                    Android.onScroll("up");
                } else if (st > lastScrollTop) {
                    Android.onScroll("down");
                }
                lastScrollTop = st;
            });
            return true;
        }

        if (attachScrollListener()) return "attached immediately";

        const observer = new MutationObserver(function(mutations) {
            if (attachScrollListener()) {
                observer.disconnect();
            }
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });

        return "observing";
    })();
            """

        fun script() = script

    }
}