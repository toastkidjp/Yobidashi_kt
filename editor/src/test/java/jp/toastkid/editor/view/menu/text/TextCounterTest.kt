package jp.toastkid.editor.view.menu.text

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.lib.ContentViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

class TextCounterTest {

    @InjectMockKs
    private lateinit var subject: TextCounter

    @MockK
    private lateinit var viewModel: EditorTabViewModel

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { viewModel.selectedText() } returns "test"
        every { contentViewModel.snackShort(any<String>()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        subject.invoke(viewModel, contentViewModel)

        verify { viewModel.selectedText() }
        verify { contentViewModel.snackShort(any<String>()) }
    }

}