/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview

import android.webkit.WebView
import androidx.collection.LruCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class WebViewPoolTest {

    private lateinit var webViewPool: WebViewPool

    @Before
    fun setUp() {
        mockkConstructor(LruCache::class)
        every { anyConstructed<LruCache<String, WebView>>().get(any()) }.returns(mockk())

        webViewPool = WebViewPool(3)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        assertNull(webViewPool.get(null))
    }

    @Test
    fun test2() {
        assertNotNull(webViewPool.get("test"))
    }

}