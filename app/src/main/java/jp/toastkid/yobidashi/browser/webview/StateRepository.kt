/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.os.Bundle
import android.os.Parcel
import android.webkit.WebView
import okio.Okio
import java.io.File

/**
 * @author toastkidjp
 */
class StateRepository(private val dir: File) {

    fun save(id: String, webView: WebView) {
        val bundle = Bundle()
        webView.saveState(bundle)

        val states = read()
        states.putBundle(id, bundle)
        write(states)
    }

    fun load(id: String, webView: WebView) {
        val states = read()
        val state = states.getBundle(id) ?: return
        webView.restoreState(state)
    }

    private fun read(): Bundle {
        val bundle = Bundle()
        val file = File(dir, FILE_NAME)
        if (!file.exists()) {
            return bundle
        }

        Okio.buffer(Okio.source(file))
                .use {
                    val readByteArray = it.readByteArray()
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(readByteArray, 0, readByteArray.size)
                    bundle.readFromParcel(parcel)
                }
        return bundle
    }

    private fun write(bundle: Bundle) {
        val parcel = Parcel.obtain()
        bundle.writeToParcel(parcel, Bundle.PARCELABLE_WRITE_RETURN_VALUE)
        Okio.buffer(Okio.sink(File(dir, FILE_NAME)))
                .use { it.write(parcel.marshall()) }
    }

    companion object {
        private const val FILE_NAME = "states"
    }
}