package jp.toastkid.yobidashi.browser.webview

import android.os.Build
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.util.ReflectionHelpers

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
    fun testIsNotFeatureSupported() {
        every { WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) }.answers { false }
        every { webView.getSettings() }.answers { mockk() }

        darkModeApplier.invoke(webView, true)

        verify(exactly = 0) { webView.getSettings() }
    }

    @Test
    fun testIsFeatureSupported() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.Q)

        every { WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) }.answers { true }
        mockkStatic(WebSettingsCompat::class)
        every { WebSettingsCompat.setAlgorithmicDarkeningAllowed(any(), any()) }.answers { Unit }
        every { webView.getSettings() }.answers { mockk() }

        darkModeApplier.invoke(webView, true)

        verify(exactly = 1) { webView.getSettings() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}