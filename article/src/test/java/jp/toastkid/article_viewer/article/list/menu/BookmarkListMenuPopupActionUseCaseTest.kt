package jp.toastkid.article_viewer.article.list.menu

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class BookmarkListMenuPopupActionUseCaseTest {

    private lateinit var useCase: BookmarkListMenuPopupActionUseCase

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var deleted: () -> Unit

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = BookmarkListMenuPopupActionUseCase(bookmarkRepository, deleted, mainDispatcher, ioDispatcher)
        coEvery { deleted() }.answers { Unit }
        coEvery { bookmarkRepository.delete(any()) }.answers { Unit }
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