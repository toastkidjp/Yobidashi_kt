package jp.toastkid.article_viewer.article.detail

import android.widget.TextView
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.noties.markwon.Markwon
import jp.toastkid.article_viewer.article.ArticleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ContentLoaderUseCaseTest {

    @InjectMockKs
    private lateinit var contentLoaderUseCase: ContentLoaderUseCase

    @MockK
    private lateinit var repository: ArticleRepository

    @MockK
    private lateinit var markwon: Markwon

    @MockK
    private lateinit var contentView: TextView

    @MockK
    private lateinit var subheads: MutableList<String>

    @MockK
    private lateinit var linkGeneratorService: LinkGeneratorService

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { markwon.setMarkdown(any(), any()) }.just(Runs)
        coEvery { linkGeneratorService.invoke(any()) }.just(Runs)
    }

    @Test
    fun test() {
        coEvery { repository.findContentByTitle(any()) }.returns("test")

        contentLoaderUseCase.invoke("test")

        coVerify(exactly = 1) { repository.findContentByTitle(any()) }
        coVerify(exactly = 1) { markwon.setMarkdown(any(), any()) }
        coVerify(exactly = 1) { linkGeneratorService.invoke(any()) }
    }

    @Test
    fun testContentIsNull() {
        coEvery { repository.findContentByTitle(any()) }.returns(null)

        contentLoaderUseCase.invoke("test")
        coVerify(exactly = 1) { repository.findContentByTitle(any()) }
        coVerify(exactly = 0) { markwon.setMarkdown(any(), any()) }
        coVerify(exactly = 0) { linkGeneratorService.invoke(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}