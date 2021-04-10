package jp.toastkid.yobidashi.browser.page_information

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.core.os.bundleOf
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier

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

        val emptyField = Bundle::class.java.getField("EMPTY")
        emptyField.isAccessible = true
        Field::class.java.getDeclaredField("modifiers").also {
            it.isAccessible = true
            it.set(emptyField, emptyField.modifiers and Modifier.FINAL.inv())
        }
        emptyField.set(null, bundleOf())
    }

    @Test
    fun testNull() {
        assertSame(Bundle.EMPTY, PageInformationExtractor().invoke(null))

        verify(exactly = 0) { webView.getUrl() }
        verify(exactly = 0) { webView.getFavicon() }
        verify(exactly = 0) { webView.getTitle() }
    }

    @Test
    fun testUrlIsNull() {
        every { webView.getUrl() }.returns(null)

        assertSame(Bundle.EMPTY, PageInformationExtractor().invoke(webView))

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