package jp.toastkid.web.webview

import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class DarkModeApplierTest {

    @InjectMockKs
    private lateinit var darkModeApplier: DarkModeApplier

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(WebViewFeature::class)
    }

    @Test
    fun testIsNotSupportedOs() {
        darkModeApplier.invoke(webView, true)

        verify(exactly = 0) { webView.settings }
    }

    @Test
    fun testIsNotFeatureSupported() {
        every { WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) }.answers { false }
        every { webView.getSettings() }.answers { mockk() }

        darkModeApplier.invoke(webView, true)

        verify(exactly = 0) { webView.getSettings() }
    }

    @Test
    fun testIsFeatureSupported() {
        every { WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) }.answers { true }
        mockkStatic(WebSettingsCompat::class)
        every { WebSettingsCompat.setAlgorithmicDarkeningAllowed(any(), any()) }.just(Runs)
        every { webView.getSettings() }.answers { mockk() }

        darkModeApplier.invoke(webView, true)

        verify(exactly = 1) { webView.getSettings() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}