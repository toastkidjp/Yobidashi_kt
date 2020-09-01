package jp.toastkid.yobidashi.main

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import jp.toastkid.yobidashi.main.launch.APP_LAUNCHER
import jp.toastkid.yobidashi.main.launch.BARCODE_READER
import jp.toastkid.yobidashi.main.launch.BOOKMARK
import jp.toastkid.yobidashi.main.launch.MainActivityIntentFactory
import jp.toastkid.yobidashi.main.launch.SEARCH
import jp.toastkid.yobidashi.main.launch.SETTING
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class MainActivityIntentFactoryTest {

    private lateinit var mainActivityIntentFactory: MainActivityIntentFactory

    @Before
    fun setUp() {
        mainActivityIntentFactory = MainActivityIntentFactory()
        mockkConstructor(Intent::class)
    }

    @Test
    fun browser() {
        every { anyConstructed<Intent>().setAction(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().setData(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().addFlags(any()) }.answers { mockk() }

        mainActivityIntentFactory.browser(mockk(), mockk())

        verify(exactly = 1) { anyConstructed<Intent>().setAction(Intent.ACTION_VIEW) }
        verify(exactly = 1) { anyConstructed<Intent>().setData(any()) }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

    @Test
    fun barcodeReader() {
        every { anyConstructed<Intent>().setAction(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().addFlags(any()) }.answers { mockk() }

        mainActivityIntentFactory.barcodeReader(mockk())

        verify(exactly = 1) { anyConstructed<Intent>().setAction(BARCODE_READER) }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

    @Test
    fun launcher() {
        every { anyConstructed<Intent>().setAction(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().addFlags(any()) }.answers { mockk() }

        mainActivityIntentFactory.launcher(mockk())

        verify(exactly = 1) { anyConstructed<Intent>().setAction(APP_LAUNCHER) }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

    @Test
    fun bookmark() {
        every { anyConstructed<Intent>().setAction(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().addFlags(any()) }.answers { mockk() }

        mainActivityIntentFactory.bookmark(mockk())

        verify(exactly = 1) { anyConstructed<Intent>().setAction(BOOKMARK) }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

    @Test
    fun search() {
        every { anyConstructed<Intent>().setAction(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().addFlags(any()) }.answers { mockk() }

        mainActivityIntentFactory.search(mockk())

        verify(exactly = 1) { anyConstructed<Intent>().setAction(SEARCH) }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

    @Test
    fun setting() {
        every { anyConstructed<Intent>().setAction(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().addFlags(any()) }.answers { mockk() }

        mainActivityIntentFactory.setting(mockk())

        verify(exactly = 1) { anyConstructed<Intent>().setAction(SETTING) }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }
}