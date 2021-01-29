package jp.toastkid.yobidashi.libs.network

import android.webkit.CookieManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class WebViewCookieHandlerTest {

    @MockK
    private lateinit var cookieManager: CookieManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { cookieManager.setCookie(any(), any()) }.answers { Unit }

        mockkStatic(CookieManager::class)
        every { CookieManager.getInstance() }.answers { cookieManager }
    }

    @Test
    fun testSaveFromResponseNoneCookie() {
        WebViewCookieHandler.saveFromResponse("https://www.yahoo.co.jp".toHttpUrl(), mutableListOf())

        verify(exactly = 1) { CookieManager.getInstance() }
        verify(exactly = 0) { cookieManager.setCookie(any(), any()) }
    }

    @Test
    fun testSaveFromResponse() {
        WebViewCookieHandler.saveFromResponse(
                "https://www.yahoo.co.jp".toHttpUrl(),
                mutableListOf(Cookie.Builder().domain("www.yahoo.co.jp").name("test").value("test").build())
        )

        verify(exactly = 1) { CookieManager.getInstance() }
        verify(exactly = 1) { cookieManager.setCookie(any(), any()) }
    }

    @Test
    fun testLoadForRequest() {
        every { cookieManager.getCookie(any()) }.returns("test")
        mockkObject(Cookie)
        every { Cookie.parse(any(), any()) }.returns(mockk())

        WebViewCookieHandler.loadForRequest("https://www.yahoo.co.jp".toHttpUrl())

        verify(exactly = 1) { CookieManager.getInstance() }
        verify(exactly = 1) { cookieManager.getCookie(any()) }
        verify(exactly = 1) { Cookie.parse(any(), any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}