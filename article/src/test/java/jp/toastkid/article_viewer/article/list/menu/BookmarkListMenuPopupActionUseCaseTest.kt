package jp.toastkid.article_viewer.article.list.menu

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import jp.toastkid.lib.clip.Clipboard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class BookmarkListMenuPopupActionUseCaseTest {

    @InjectMockKs
    private lateinit var useCase: BookmarkListMenuPopupActionUseCase

    @MockK
    private lateinit var articleRepository: ArticleRepository

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var deleted: () -> Unit

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { deleted() }.just(Runs)
        coEvery { articleRepository.findContentById(any()) } returns "test-content"
        coEvery { bookmarkRepository.delete(any()) }.just(Runs)

        mockkObject(Clipboard)
        every { Clipboard.clip(any(), any()) } just Runs
    }

    @Test
    fun copyTitle() {
        useCase.copyTitle(mockk(), "test")

        verify { Clipboard.clip(any(), any()) }
    }

    @Test
    fun addToBookmark() {
        useCase.addToBookmark(1)

        coVerify(exactly = 0) { bookmarkRepository.delete(any()) }
        coVerify(exactly = 0) { deleted() }
    }

    @Test
    fun delete() {
        useCase.delete(1)

        coVerify(exactly = 1) { bookmarkRepository.delete(any()) }
        coVerify(exactly = 1) { deleted() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}