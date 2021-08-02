package jp.toastkid.yobidashi.main.launch

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
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

        every { randomWikipedia.fetchWithAction(any()) }.answers { Unit }
    }

    @Test
    fun test() {
        randomWikipediaUseCase.invoke()

        verify (exactly = 1) { randomWikipedia.fetchWithAction(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}