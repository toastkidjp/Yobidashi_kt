package jp.toastkid.yobidashi.main.launch

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class RandomWikipediaUseCaseTest {

    @InjectMockKs
    private lateinit var randomWikipediaUseCase: RandomWikipediaUseCase

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var openNewWebTab: (Uri) -> Unit

    @MockK
    private lateinit var stringFinder: (Int, String) -> String

    @MockK
    private lateinit var randomWikipedia: RandomWikipedia

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        Dispatchers.setMain(Dispatchers.Unconfined)

        coEvery { randomWikipedia.fetchWithAction(any()) }.answers { Unit }

        coEvery { openNewWebTab.invoke(any()) }.answers { Unit }
        coEvery { contentViewModel.snackShort(any<String>()) }.answers { Unit }
        coEvery { stringFinder(any(), any()) }.answers { "test" }
    }

    @Test
    fun test() {
        randomWikipediaUseCase.invoke()

        coVerify (exactly = 1) { randomWikipedia.fetchWithAction(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

}