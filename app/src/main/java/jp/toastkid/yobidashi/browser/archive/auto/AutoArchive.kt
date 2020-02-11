/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.archive.auto

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.libs.storage.StorageWrapper

/**
 * @author toastkidjp
 */
class AutoArchive(private val filesDir: StorageWrapper) {

    fun save(webView: WebView?, tabId: String) {
        webView?.saveWebArchive(filesDir.assignNewFile("$tabId$EXTENSION").absolutePath)
    }

    fun delete(tabId: String) {
        filesDir.delete("$tabId$EXTENSION")
    }

    fun load(webView: WebView?, tabId: String, callback: () -> Unit) {
        val file = filesDir.findByName("$tabId$EXTENSION") ?: return
        webView?.let {
            it.loadUrl(Uri.fromFile(file).toString())
            callback()
        }
    }

    companion object {
        private const val FOLDER = "auto_archives"

        private const val EXTENSION = ".mht"

        fun make(context: Context) = AutoArchive(FilesDir(context, FOLDER))
    }
}