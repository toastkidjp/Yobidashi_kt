package jp.toastkid.yobidashi.libs.ad

import com.google.android.gms.ads.AdView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jp.toastkid.lib.AppBarViewModel
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class AdServiceTest {

    @MockK
    private lateinit var adViewFactory: AdViewFactory

    @MockK
    private lateinit var adView: AdView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun load() {
        every { adView.setAdSize(any()) }.answers { Unit }
        every { adView.loadAd(any()) }.answers { Unit }
        every { adViewFactory.invoke(any()) }.answers { adView }
        val service = AdService(adViewFactory, mockk())

        service.load()

        verify(atLeast = 1) { adView.loadAd(any()) }
    }

    @Test
    fun sendWith() {
        every { adView.setAdSize(any()) }.answers { Unit }
        every { adViewFactory.invoke(any()) }.answers { adView }
        val service = AdService(adViewFactory, mockk())

        val appBarViewModel = mockk<AppBarViewModel>()
        every { appBarViewModel.replace(any()) }.answers { Unit }

        service.sendWith(appBarViewModel)

        verify(atLeast = 1) { appBarViewModel.replace(adView) }
    }

    @Test
    fun destroy() {
        every { adView.setAdSize(any()) }.answers { Unit }
        every { adView.destroy() }.answers { Unit }
        every { adViewFactory.invoke(any()) }.answers { adView }
        val service = AdService(adViewFactory, mockk())

        val appBarViewModel = mockk<AppBarViewModel>()
        every { appBarViewModel.replace(any()) }.answers { Unit }

        service.destroy()

        verify(atLeast = 1) { adView.destroy() }
    }
}