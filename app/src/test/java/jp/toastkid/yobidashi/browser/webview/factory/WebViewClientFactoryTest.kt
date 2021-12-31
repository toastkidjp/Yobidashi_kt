/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.rss.suggestion.RssAddingSuggestion
import org.junit.After
import org.junit.Before
import org.junit.Test

class WebViewClientFactoryTest {

    @InjectMockKs
    private lateinit var webViewClientFactory: WebViewClientFactory

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var adRemover: AdRemover

    @MockK
    private lateinit var faviconApplier: FaviconApplier

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var browserHeaderViewModel: BrowserHeaderViewModel

    @MockK
    private lateinit var rssAddingSuggestion: RssAddingSuggestion

    @MockK
    private lateinit var loadingViewModel: LoadingViewModel

    @MockK
    private lateinit var currentView: () -> WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        webViewClientFactory.invoke()
    }

}