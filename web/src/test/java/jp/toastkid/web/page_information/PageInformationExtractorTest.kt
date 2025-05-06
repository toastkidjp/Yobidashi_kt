package jp.toastkid.web.page_information

import android.webkit.CookieManager
import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class PageInformationExtractorTest {

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { webView.getFavicon() }.returns(mockk())
        every { webView.getTitle() }.returns("test")
    }

    @Test
    fun testNull() {
        assertTrue(PageInformationExtractor().invoke(null).isEmpty())

        verify(exactly = 0) { webView.getUrl() }
        verify(exactly = 0) { webView.getFavicon() }
        verify(exactly = 0) { webView.getTitle() }
    }

    @Test
    fun testUrlIsNull() {
        every { webView.getUrl() }.returns(null)

        assertTrue(PageInformationExtractor().invoke(webView).isEmpty())

        verify(exactly = 1) { webView.getUrl() }
        verify(exactly = 0) { webView.getFavicon() }
        verify(exactly = 0) { webView.getTitle() }
    }

    @Test
    fun test() {
        every { webView.getUrl() }.returns("https://www.yahoo.co.jp")
        val cookieManager = mockk<CookieManager>()
        every { cookieManager.getCookie(any()) }.returns("test")
        mockkStatic(CookieManager::class)
        every { CookieManager.getInstance() }.returns(cookieManager)

        PageInformationExtractor().invoke(webView)

        verify(exactly = 1) { webView.getUrl() }
        verify(exactly = 1) { webView.getFavicon() }
        verify(exactly = 1) { webView.getTitle() }
        verify(exactly = 1) { CookieManager.getInstance() }
        verify(exactly = 1) { cookieManager.getCookie(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}