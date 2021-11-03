package jp.toastkid.yobidashi.browser

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class HtmlSourceExtractionUseCaseTest {

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { webView.url }.returns("https://www.yahoo.co.jp")
        every { webView.evaluateJavascript(any(), any()) }.answers { Unit }

        HtmlSourceExtractionUseCase().invoke(webView, mockk())

        verify (exactly = 1) { webView.evaluateJavascript(any(), any()) }
    }

    @Test
    fun testExceptingCase() {
        every { webView.url }.returns("https://accounts.google.com/signin")
        every { webView.evaluateJavascript(any(), any()) }.answers { Unit }

        HtmlSourceExtractionUseCase().invoke(webView, mockk())

        verify (exactly = 0) { webView.evaluateJavascript(any(), any()) }
    }
}