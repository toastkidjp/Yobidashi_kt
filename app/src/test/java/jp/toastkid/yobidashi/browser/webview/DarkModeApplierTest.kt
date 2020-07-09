package jp.toastkid.yobidashi.browser.webview

import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class DarkModeApplierTest {

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(WebViewFeature::class)
    }

    @Test
    fun testIsNotFeatureSupported() {
        every { WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) }.answers { false }
        every { webView.getSettings() }.answers { mockk() }

        DarkModeApplier().invoke(webView, true)

        verify(exactly = 0) { webView.getSettings() }
    }

    @Test
    fun testIsFeatureSupported() {
        every { WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) }.answers { true }
        mockkStatic(WebSettingsCompat::class)
        every { WebSettingsCompat.setForceDark(any(), any()) }.answers { Unit }
        every { webView.getSettings() }.answers { mockk() }

        DarkModeApplier().invoke(webView, true)

        verify(exactly = 1) { webView.getSettings() }
    }

}