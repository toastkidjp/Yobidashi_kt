package jp.toastkid.yobidashi.main.launch

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.wikipedia.random.UrlDecider
import jp.toastkid.yobidashi.wikipedia.random.WikipediaApi
import jp.toastkid.yobidashi.wikipedia.random.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * @author toastkidjp
 */
class RandomWikipediaUseCaseTest {

    private lateinit var randomWikipediaUseCase: RandomWikipediaUseCase

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var openNewWebTab: (Uri) -> Unit

    @MockK
    private lateinit var stringFinder: (Int, String) -> String

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        Dispatchers.setMain(Dispatchers.Unconfined)

        mockkConstructor(WikipediaApi::class)
        coEvery { anyConstructed<WikipediaApi>().invoke() }
                .answers { arrayOf(Article(1L, 0, "test")) }

        mockkConstructor(UrlDecider::class)
        coEvery { anyConstructed<UrlDecider>().invoke() }
                .answers { "https://${Locale.getDefault().language}.wikipedia.org/" }

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.answers { mockk() }

        every { openNewWebTab.invoke(any()) }.answers { Unit }
        every { contentViewModel.snackShort(any<String>()) }.answers { Unit }
        every { stringFinder(any(), any()) }.answers { "test" }

        randomWikipediaUseCase = RandomWikipediaUseCase(contentViewModel, openNewWebTab, stringFinder)
    }

    @Test
    fun test() {
        randomWikipediaUseCase.invoke()

        verify(exactly = 1) { openNewWebTab.invoke(any()) }
        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 1) { stringFinder(any(), any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}