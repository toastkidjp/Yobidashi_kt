package jp.toastkid.todo.view.board

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.todo.model.TodoTask
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TaskAddingUseCaseTest {

    private lateinit var taskAddingUseCase: TaskAddingUseCase

    @MockK
    private lateinit var tasks: MutableList<TodoTask>

    @MockK
    private lateinit var board: ViewGroup

    @MockK
    private lateinit var boardItemViewFactory: BoardItemViewFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { tasks.add(any()) }.answers { true }
        every { board.addView(any()) }.answers { Unit }

        taskAddingUseCase = TaskAddingUseCase(Color.BLACK, tasks, board, boardItemViewFactory)
    }

    @Test
    fun test() {
        val itemView = mockk<View>()
        every { itemView.setTag(any()) }.answers { Unit }
        every { boardItemViewFactory.invoke(any(), any(), any()) }.answers { itemView }

        val task = TodoTask(0)
        taskAddingUseCase.invoke(task)

        verify(exactly = 1) { tasks.add(task) }
        verify(exactly = 1) { boardItemViewFactory.invoke(board, task, Color.BLACK) }
        verify(exactly = 1) { itemView.setTag(task.id) }
        verify(exactly = 1) { board.addView(itemView) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}