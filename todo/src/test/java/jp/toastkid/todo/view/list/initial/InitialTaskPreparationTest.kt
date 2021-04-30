package jp.toastkid.todo.view.list.initial

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.todo.data.TodoTaskDataAccessor
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class InitialTaskPreparationTest {

    private lateinit var preparation: InitialTaskPreparation

    @MockK
    private lateinit var repository: TodoTaskDataAccessor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        preparation = InitialTaskPreparation(repository)
        every { repository.insert(any()) }.answers { Unit }
    }

    @Test
    fun test() {
        preparation.invoke()

        verify(exactly = 2) { repository.insert(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}