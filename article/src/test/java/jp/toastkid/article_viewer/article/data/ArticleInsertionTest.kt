package jp.toastkid.article_viewer.article.data

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import jp.toastkid.article_viewer.article.ArticleRepository
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ArticleInsertionTest {

    private lateinit var articleInsertion: ArticleInsertion

    @MockK
    private lateinit var repository: ArticleRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val context = mockk<Context>()

        val database = mockk<AppDatabase>()
        mockkObject(AppDatabase)
        every { AppDatabase.find(any()) }.answers { database }
        every { database.articleRepository() }.answers { repository }

        articleInsertion = ArticleInsertion(context, Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        unmockkAll()
        AppDatabase.clearInstance()
    }

    @Test
    fun testTitleIsNull() {
        coEvery { repository.insert(any()) }.just(Runs)

        articleInsertion.invoke(null, "test")

        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun testContentIsBlank() {
        coEvery { repository.insert(any()) }.answers { Unit }

        articleInsertion.invoke("test", "  ")

        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun test() {
        coEvery { repository.insert(any()) }.answers { Unit }

        articleInsertion.invoke("test", "test")

        coVerify(exactly = 1) { repository.insert(any()) }
    }

}