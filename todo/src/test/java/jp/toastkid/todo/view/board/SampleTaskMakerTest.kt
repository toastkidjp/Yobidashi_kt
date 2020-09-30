package jp.toastkid.todo.view.board

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class SampleTaskMakerTest {

    @Test
    fun test() {
        val task = SampleTaskMaker().invoke()
        assertTrue(task.description.isNotBlank())
        assertFalse(task.done)
        assertTrue(task.x != 0f)
        assertTrue(task.y != 0f)
    }

}