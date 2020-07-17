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
import jp.toastkid.lib.file.ExtensionRemover
import jp.toastkid.lib.storage.CacheDir
import jp.toastkid.lib.storage.StorageWrapper

/**
 * @author toastkidjp
 */
class AutoArchive(private val filesDir: StorageWrapper) {

    private val extensionRemover = ExtensionRemover()

    fun save(webView: WebView?, id: String?) {
        webView?.saveWebArchive(filesDir.assignNewFile("$id$EXTENSION").absolutePath)
    }

    fun delete(tabId: String?) {
        if (tabId == null) {
            return
        }
        filesDir.delete("$tabId$EXTENSION")
    }

    fun deleteUnused(archiveIds: Collection<String>) {
        filesDir.listFiles()
                .filter { !archiveIds.contains(extensionRemover(it.name)) }
                .forEach { it.delete() }
    }

    fun load(webView: WebView?, id: String?, callback: () -> Unit) {
        if (id == null) {
            return
        }

        val file = filesDir.findByName("$id$EXTENSION") ?: return
        webView?.let {
            it.loadUrl(Uri.fromFile(file).toString())
            callback()
        }
    }

    companion object {
        private const val FOLDER = "auto_archives"

        private const val EXTENSION = ".mht"

        fun make(context: Context) = AutoArchive(CacheDir(context, FOLDER))

        fun shouldNotUpdateTab(url: String?) = url?.contains("/files/auto_archives/") == true
    }
}