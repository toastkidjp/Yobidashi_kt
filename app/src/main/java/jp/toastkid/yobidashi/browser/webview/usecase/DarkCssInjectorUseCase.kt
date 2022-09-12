/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.os.Build
import android.webkit.WebView
import jp.toastkid.lib.preference.PreferenceApplier

class DarkCssInjectorUseCase {

    operator fun invoke(webView: WebView) {
        webView.evaluateJavascript(CSS_INJECTOR_SCRIPT) { }
    }

    companion object {
        private const val CSS_INJECTOR_SCRIPT = """
                    (function () {
                        'use strict';
                        
                        var headElement = document.getElementsByTagName('head')[0],
                            styleElement = document.createElement('style');
                        
                        styleElement.type = 'text/css';
                        
                        var cssText = 
                            'html{      filter: invert(1) hue-rotate(180deg);  }'
                               + 'html img, .Image, .ytp-cued-thumbnail-overlay-image, .EmbeddedImage,'
                               + '.theme-Kisekae__backgroundImage--headerChar {'
                               +      'filter: invert(1) hue-rotate(180deg);'
                               + '}';
                        
                        if (styleElement.styleSheet) {
                            styleElement.styleSheet.cssText = cssText;
                        } else {
                            styleElement.appendChild(document.createTextNode(cssText));
                        }

                        headElement.appendChild(styleElement);
                    }());
                """

        fun isTarget(preferenceApplier: PreferenceApplier) =
            preferenceApplier.useDarkMode() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    }
}