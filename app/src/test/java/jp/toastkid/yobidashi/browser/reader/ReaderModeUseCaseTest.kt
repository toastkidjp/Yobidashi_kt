package jp.toastkid.yobidashi.browser.reader

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
class ReaderModeUseCaseTest {

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { webView.evaluateJavascript(any(), any()) }.answers { Unit }

        ReaderModeUseCase().invoke(webView, mockk())

        verify(exactly = 1) { webView.evaluateJavascript(any(), any()) }
    }

}