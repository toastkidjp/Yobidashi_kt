package jp.toastkid.article_viewer.article.list

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ListLoaderUseCaseTest {

    @MockK
    private lateinit var adapter: Adapter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDispose() {
        ListLoaderUseCase(adapter, Dispatchers.Unconfined).dispose()
    }
}