/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.block

import android.content.res.AssetManager
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * This class is "shouldInterceptRequest" delegation for blocking load AD content.
 *
 * @author toastkidjp
 */
class AdRemover(inputStream: InputStream) {

    /**
     * Blacklist of AD hosts.
     */
    private var blackList: Set<String> =
            inputStream.bufferedReader().use { bufferedSource ->
                bufferedSource.readLine().split("\n")
                        .filter { it.isNotBlank() }
                        .map { it.trim() }
                        .toHashSet()
            }

    /**
     * Invoke this class.
     *
     * @param requestUrl URL for loading ad content.
     */
    operator fun invoke(requestUrl: String): WebResourceResponse? =
            if (isAdHost(requestUrl)) EMPTY else null

    private fun isAdHost(url: String): Boolean = blackList.any { url.contains(it) }

    companion object {

        /**
         * Dummy content.
         */
        private val EMPTY: WebResourceResponse = WebResourceResponse(
                "text/plain",
                "UTF-8",
                ByteArrayInputStream(byteArrayOf())
        )

        fun make(assetManager: AssetManager): AdRemover {
            return AdRemover(assetManager.open("ad_hosts.txt"))
        }
    }
}