/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.lib

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * For sharing [WebView]'s cookie.
 *
 * @author toastkidjp
 */
class WebViewCookieHandler : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        val cookieManager = CookieManager.getInstance()
        cookies.forEach { cookieManager.setCookie(urlString, it.toString()) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url.toString()
        val cookieManager = CookieManager.getInstance()
        val cookiesString = cookieManager.getCookie(urlString)

        return if (cookiesString != null && cookiesString.isNotEmpty()) {
            cookiesString
                    .split(DELIMITER)
                    .mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

    companion object {

        private const val DELIMITER = ";"

    }

}