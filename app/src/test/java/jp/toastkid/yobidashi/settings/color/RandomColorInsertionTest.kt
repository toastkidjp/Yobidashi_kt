package jp.toastkid.yobidashi.settings.color

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class RandomColorInsertionTest {

    @Test
    fun test() {
        val repository = mockk<SavedColorRepository>()
        every { repository.add(any()) }.answers { Unit }

        val afterInserted = mockk<() -> Unit>()
        every { afterInserted.invoke() }.answers { Unit }

        RandomColorInsertion(repository).invoke(afterInserted)

        verify(exactly = 1) { repository.add(any()) }
        verify(exactly = 1) { afterInserted.invoke() }
    }
}