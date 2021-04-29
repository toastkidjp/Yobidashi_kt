/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.usecase

import android.view.animation.Animation
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.collection.LruCache
import androidx.fragment.app.FragmentActivity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebSettingApplier
import jp.toastkid.yobidashi.browser.webview.WebViewPool
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class WebViewReplacementUseCaseTest {

    @InjectMockKs
    private lateinit var webViewReplacementUseCase: WebViewReplacementUseCase

    @MockK
    private lateinit var webViewContainer: FrameLayout

    @MockK
    private lateinit var webViewStateUseCase: WebViewStateUseCase

    @MockK
    private lateinit var makeWebView: () -> WebView

    @MockK
    private lateinit var browserHeaderViewModel: BrowserHeaderViewModel

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var slideUpFromBottom: Animation

    @MockK
    private lateinit var darkThemeApplier: DarkModeApplier

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { webViewContainer.getChildCount() }.returns(0) // TODO Other case
        every { webViewContainer.getChildAt(any()) }.returns(webView)
        every { webViewContainer.addView(any()) }.returns(Unit)
        every { webViewContainer.removeAllViews() }.returns(Unit)
        every { webViewContainer.startAnimation(any()) }.returns(Unit)
        every { webViewContainer.getContext() }.returns(mockk<FragmentActivity>()) // TODO Other case

        every { webView.onResume() }.returns(Unit)
        every { webView.getParent() }.returns(webViewContainer)
        every { webView.getSettings() }.returns(mockk())
        every { webView.canGoBack() }.returns(false)
        every { webView.canGoForward() }.returns(false)
        every { webView.getTitle() }.returns("test title")
        every { webView.getUrl() }.returns("https://www.yahoo.co.jp")
        every { darkThemeApplier.invoke(any(), any()) }.returns(Unit)
        every { preferenceApplier.useDarkMode() }.returns(true)
        every { preferenceApplier.browserScreenMode() }.returns("fixed")

        every { browserHeaderViewModel.setBackButtonEnability(any()) }.returns(Unit)
        every { browserHeaderViewModel.setForwardButtonEnability(any()) }.returns(Unit)
        every { browserHeaderViewModel.nextTitle(any()) }.returns(Unit)
        every { browserHeaderViewModel.nextUrl(any()) }.returns(Unit)
        every { makeWebView.invoke() }.returns(webView)
        every { webViewStateUseCase.restore(any(), any()) }.returns(Unit)

        mockkObject(ScreenMode)
        every { ScreenMode.find(any()) }.returns(ScreenMode.FULL_SCREEN) // TODO Other case

        mockkConstructor(WebViewPool::class)
        mockkConstructor(LruCache::class)

        mockkObject(GlobalWebViewPool)
        every { GlobalWebViewPool.containsKey(any()) }.returns(false) // TODO true case
        every { GlobalWebViewPool.put(any(), any()) }.returns(Unit)
        every { GlobalWebViewPool.get(any()) }.returns(webView)
        every { GlobalWebViewPool.getLatest() }.returns(webView)

        mockkConstructor(WebSettingApplier::class)
        every { anyConstructed<WebSettingApplier>().invoke(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        webViewReplacementUseCase.invoke("test-id")

        verify(exactly = 1) { webViewContainer.getChildCount() }
        verify(exactly = 0) { webViewContainer.getChildAt(any()) }
        verify(exactly = 1) { webViewContainer.getContext() }
        verify(atLeast = 1) { webView.onResume() }
        verify(atLeast = 1) { webView.getParent() }
        verify(atLeast = 1) { webView.getSettings() }
        verify(atLeast = 1) { webView.canGoBack() }
        verify(atLeast = 1) { webView.canGoForward() }
        verify(atLeast = 1) { webView.getTitle() }
        verify(atLeast = 1) { webView.getUrl() }
        verify(exactly = 1) { darkThemeApplier.invoke(any(), any()) }
        verify(exactly = 1) { preferenceApplier.useDarkMode() }
        verify(exactly = 1) { preferenceApplier.browserScreenMode() }
        verify(exactly = 1) { browserHeaderViewModel.setBackButtonEnability(any()) }
        verify(exactly = 1) { browserHeaderViewModel.setForwardButtonEnability(any()) }
        verify(exactly = 1) { browserHeaderViewModel.nextTitle(any()) }
        verify(exactly = 1) { browserHeaderViewModel.nextUrl(any()) }
        verify(exactly = 1) { makeWebView.invoke() }
        verify(exactly = 1) { webViewStateUseCase.restore(any(), any()) }
        verify(exactly = 1) { GlobalWebViewPool.put(any(), any()) }
        verify(exactly = 1) { GlobalWebViewPool.get(any()) }
        verify(exactly = 1) { GlobalWebViewPool.getLatest() }
        verify(exactly = 1) { anyConstructed<WebSettingApplier>().invoke(any()) }
    }

}