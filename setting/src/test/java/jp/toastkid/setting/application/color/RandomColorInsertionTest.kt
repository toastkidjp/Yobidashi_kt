package jp.toastkid.setting.application.color

import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jp.toastkid.yobidashi.settings.color.SavedColorRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Test

/**
 * @author toastkidjp
 */
class RandomColorInsertionTest {

    @Test
    fun test() {
        val repository = mockk<SavedColorRepository>()
        every { repository.add(any()) }.answers { 1L }

        val afterInserted = mockk<() -> Unit>()
        every { afterInserted.invoke() } just Runs

        RandomColorInsertion(repository).invoke(afterInserted, Dispatchers.Unconfined)

        coVerify(exactly = 1) { repository.add(any()) }
        coVerify(exactly = 1) { afterInserted.invoke() }
    }
}