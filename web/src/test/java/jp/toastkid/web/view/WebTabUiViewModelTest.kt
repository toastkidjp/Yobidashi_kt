package jp.toastkid.web.view

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.web.video.seek.BackwardSeek
import org.junit.After
import org.junit.Before
import org.junit.Test

class WebTabUiViewModelTest {

    private lateinit var subject: WebTabUiViewModel

    @MockK
    private lateinit var backwardSeek: BackwardSeek

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { backwardSeek.isTarget(any()) } returns false

        subject = WebTabUiViewModel(backwardSeek)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun stopProgress() {
    }

    @Test
    fun icon() {
    }

    @Test
    fun newIcon() {
    }

    @Test
    fun resetIcon() {
    }

    @Test
    fun title() {
    }

    @Test
    fun nextTitle() {
    }

    @Test
    fun url() {
    }

    @Test
    fun nextUrl() {
    }

    @Test
    fun enableForward() {
    }

    @Test
    fun setForwardButtonIsEnabled() {
    }

    @Test
    fun enableBack() {
    }

    @Test
    fun setBackButtonIsEnabled() {
    }

    @Test
    fun progress() {
    }

    @Test
    fun updateProgress() {
    }

    @Test
    fun shouldShowProgressIndicator() {
    }

    @Test
    fun openErrorDialog() {
    }

    @Test
    fun closeErrorDialog() {
    }

    @Test
    fun error() {
    }

    @Test
    fun setError() {
    }

    @Test
    fun clearError() {
    }

    @Test
    fun getLongTapActionParameters() {
    }

    @Test
    fun setLongTapParameters() {
    }

    @Test
    fun isOpenLongTapDialog() {
    }

    @Test
    fun clearLongTapParameters() {
    }

    @Test
    fun readerModeText() {
    }

    @Test
    fun showReader() {
    }

    @Test
    fun isOpenReaderMode() {
    }

    @Test
    fun closeReaderMode() {
    }

    @Test
    fun isRefreshing() {
    }

    @Test
    fun setRefreshing() {
    }

    @Test
    fun isBackwardSeekTarget() {
        subject.isBackwardSeekTarget("test")

        verify { backwardSeek.isTarget(any()) }
    }

}