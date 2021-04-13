package jp.toastkid.yobidashi.browser

import android.webkit.CookieManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class CookieCleanerCompatTest {

    @MockK
    private lateinit var cookieManager: CookieManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Config(sdk = [22])
    @Test
    fun test() {
        every { cookieManager.removeAllCookies(any()) }.answers { Unit }
        mockkStatic(CookieManager::class)
        every { CookieManager.getInstance() }.answers { cookieManager }

        CookieCleanerCompat().invoke(mockk(), mockk())

        verify(exactly = 1) { cookieManager.removeAllCookies(any()) }
    }
}