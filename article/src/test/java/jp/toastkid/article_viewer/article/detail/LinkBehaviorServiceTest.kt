package jp.toastkid.article_viewer.article.detail

import android.net.Uri
import androidx.core.net.toUri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author toastkidjp
 */
class LinkBehaviorServiceTest {

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testNullUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        every { contentViewModel.newArticle(any()) }.answers { Unit }
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel)

        linkBehaviorService.invoke(null)

        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testEmptyUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        every { contentViewModel.newArticle(any()) }.answers { Unit }
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel)

        linkBehaviorService.invoke("")

        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testWebUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        every { contentViewModel.newArticle(any()) }.answers { Unit }
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel)

        linkBehaviorService.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testArticleUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        every { contentViewModel.newArticle(any()) }.answers { Unit }
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel)

        linkBehaviorService.invoke("internal-article://yahoo")

        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 1) { contentViewModel.newArticle(any()) }
    }

}