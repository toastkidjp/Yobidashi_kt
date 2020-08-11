package jp.toastkid.article_viewer.calendar

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class DateSelectedActionServiceTest {

    @MockK
    private lateinit var repository: ArticleRepository

    @MockK
    private lateinit var viewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun test() {
        every { repository.findFirst(any()) }.answers { Article(1).also { it.title = "test" } }
        every { viewModel.newArticle(any()) }.answers { Unit }

        mockkObject(TitleFilterGenerator)
        every { TitleFilterGenerator.invoke(any(), any(), any()) }.answers { "test" }

        Dispatchers.setMain(Dispatchers.Unconfined)

        val dateSelectedActionService = DateSelectedActionService(repository, viewModel)
        dateSelectedActionService.invoke(2020, 0, 22)
        Thread.sleep(1000L)

        verify(exactly = 1) { repository.findFirst(any()) }
        verify(exactly = 1) { viewModel.newArticle(any()) }
        verify(exactly = 1) { TitleFilterGenerator.invoke(any(), any(), any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}