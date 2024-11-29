package jp.toastkid.web.webview

import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.lifecycle.ViewModelProvider
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class SelectedTextExtractorTest {

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(ViewModelProvider::class)
        every { anyConstructed<ViewModelProvider>().get(ContentViewModel::class.java) }.returns(contentViewModel)
        every { contentViewModel.snackWithAction(any(), any(), any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        val selectedTextExtractor = SelectedTextExtractor()
        val webView = mockk<WebView>()
        val slot = slot<ValueCallback<String>>()
        every { webView.evaluateJavascript(any(), capture(slot)) }.answers {  }

        selectedTextExtractor.withAction(webView, {})

        slot.captured.onReceiveValue("test")
        verify(atMost = 1) { webView.evaluateJavascript(any(), any()) }
        verify { contentViewModel wasNot called }
    }

}