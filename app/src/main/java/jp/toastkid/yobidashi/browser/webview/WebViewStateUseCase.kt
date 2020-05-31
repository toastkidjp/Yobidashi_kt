/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.webkit.WebView
import jp.toastkid.yobidashi.libs.storage.CacheDir
import okio.Okio

/**
 * @author toastkidjp
 */
class WebViewStateUseCase(private val folder: CacheDir) {

    private fun assignNewFile(name: String) = folder.assignNewFile(name)

    fun store(webView: WebView?, tabId: String) {
        val file = assignNewFile(tabId)
        if (!file.exists()) {
            file.createNewFile()
        }

        val state = Bundle()
        webView?.saveState(state)

        val parcel = Parcel.obtain()
        state.writeToParcel(parcel, 0)
        Okio.buffer(Okio.sink(file)).use { it.write(parcel.marshall()) }
        parcel.recycle()
    }

    fun restore(webView: WebView?, tabId: String?) {
        if (tabId == null) {
            return
        }

        val file = assignNewFile(tabId)
        if (!file.exists()) {
            return
        }

        val byteArray = Okio.buffer(Okio.source(file)).use { it.readByteArray() } ?: return
        file.delete()

        val parcel = Parcel.obtain()
        parcel.unmarshall(byteArray, 0, byteArray.size)

        val state = Bundle()
        state.readFromParcel(parcel)
        parcel.recycle()

        webView?.restoreState(state)
    }

    fun clean() = folder.clean()

    fun delete(tabId: String?) {
        if (tabId.isNullOrBlank()) {
            return
        }

        val stateFile = assignNewFile(tabId)
        if (stateFile.exists()) {
            stateFile.delete()
        }
    }

    fun deleteUnused(exceptionalTabIds: Collection<String>) {
        folder.listFiles()
                .filter { !exceptionalTabIds.contains(it.name) }
                .forEach { it.delete() }
    }

    companion object {

        /**
         * Directory path to screenshot.
         */
        private const val SCREENSHOT_DIR_PATH: String = "tabs/states"

        fun make(context: Context): WebViewStateUseCase {
            return WebViewStateUseCase(CacheDir(context, SCREENSHOT_DIR_PATH))
        }
    }
}