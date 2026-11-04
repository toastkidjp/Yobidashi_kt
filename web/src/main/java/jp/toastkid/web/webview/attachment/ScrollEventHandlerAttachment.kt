/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.webview.attachment

import android.webkit.JavascriptInterface

class ScrollEventHandlerAttachment(
    private val showAppBar: () -> Unit,
    private val hideAppBar: () -> Unit
) {

    @Suppress("unused")
    @JavascriptInterface
    fun onScroll(direction: String) {
        if (direction == "up") {
            showAppBar()
        } else {
            hideAppBar()
        }
    }

    companion object {

        private const val SCRIPT = """
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
                    ScrollEventHandlerAttachment.onScroll("up");
                } else if (st > lastScrollTop) {
                    ScrollEventHandlerAttachment.onScroll("down");
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

        fun script() = SCRIPT

        fun interfaceName() = "ScrollEventHandlerAttachment"

    }
}