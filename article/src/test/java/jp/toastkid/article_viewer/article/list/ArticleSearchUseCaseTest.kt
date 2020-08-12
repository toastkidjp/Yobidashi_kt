package jp.toastkid.article_viewer.article.list

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ArticleSearchUseCaseTest {

    private lateinit var articleSearchUseCase: ArticleSearchUseCase

    @MockK
    private lateinit var loaderUseCase: ListLoaderUseCase

    @MockK
    private lateinit var repository: ArticleRepository

    @MockK
    private lateinit var preferencesWrapper: PreferenceApplier

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Job::class)
        every { anyConstructed<Job>().cancel() }.answers { Unit }

        articleSearchUseCase = ArticleSearchUseCase(loaderUseCase, repository, preferencesWrapper)

        every { loaderUseCase.invoke(any()) }.answers { Unit }
        every { loaderUseCase.dispose() }.answers { Unit }
        every { repository.search(any()) }.answers { mockk() }
        every { repository.filter(any()) }.answers { mockk() }
    }

    @Test
    fun all() {
        articleSearchUseCase.all()

        verify(exactly = 1) { loaderUseCase.invoke(any()) }
    }

    @Test
    fun testSearchNull() {
        articleSearchUseCase.search(null)

        verify(exactly = 0) { loaderUseCase.invoke(any()) }
    }

    @Test
    fun testSearchEmpty() {
        articleSearchUseCase.search("")

        verify(exactly = 0) { loaderUseCase.invoke(any()) }
    }

    @Test
    fun testSearch() {
        articleSearchUseCase.search("tomato")

        verify(exactly = 1) { loaderUseCase.invoke(any()) }
    }

    @Test
    fun testFilterIsDisable() {
        every { preferencesWrapper.useTitleFilter() }.returns(false)

        articleSearchUseCase.filter(null)

        verify(exactly = 0) { loaderUseCase.invoke(any()) }
    }

    @Test
    fun testFilter() {
        every { preferencesWrapper.useTitleFilter() }.returns(true)

        articleSearchUseCase.filter("tomato")

        verify(exactly = 1) { loaderUseCase.invoke(any()) }
    }

    @Test
    fun testDispose() {
        articleSearchUseCase.dispose()

        verify(exactly = 1) { loaderUseCase.dispose() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}