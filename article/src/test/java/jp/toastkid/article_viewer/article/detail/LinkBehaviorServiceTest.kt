package jp.toastkid.article_viewer.article.detail

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LinkBehaviorServiceTest {

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @MockK
    private lateinit var exists: (String) -> Boolean

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Default)
    }

    @Test
    fun testNullUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        every { contentViewModel.newArticle(any()) }.answers { Unit }
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel, exists)

        linkBehaviorService.invoke(null)

        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testEmptyUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        every { contentViewModel.newArticle(any()) }.answers { Unit }
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel, exists)

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
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel, exists)

        linkBehaviorService.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testArticleUrl() {
        every { browserViewModel.open(any()) }.answers { Unit }
        coEvery { contentViewModel.newArticle(any()) }.answers { Unit }
        coEvery { contentViewModel.snackShort(any<String>()) }.answers { Unit }
        coEvery { exists(any()) }.answers { false }
        val linkBehaviorService = LinkBehaviorService(contentViewModel, browserViewModel, exists)

        linkBehaviorService.invoke("internal-article://yahoo")

        verify(exactly = 0) { browserViewModel.open(any()) }
        coVerify(exactly = 0) { contentViewModel.newArticle(any()) }
        coVerify(exactly = 1) { contentViewModel.snackShort(any<String>())}
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

}