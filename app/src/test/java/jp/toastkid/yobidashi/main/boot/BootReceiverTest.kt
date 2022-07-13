package jp.toastkid.yobidashi.main.boot

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class BootReceiverTest {

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getSharedPreferences(any(), any()) }.answers { mockk() }

        mockkConstructor(PreferenceApplier::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testPreferenceTrue() {
        every { anyConstructed<PreferenceApplier>().useNotificationWidget() }.answers { true }

        BootReceiver().onReceive(context, mockk())

        verify(exactly = 1) { anyConstructed<PreferenceApplier>().useNotificationWidget() }
    }

    @Test
    fun testPreferenceFalse() {
        every { anyConstructed<PreferenceApplier>().useNotificationWidget() }.answers { false }

        BootReceiver().onReceive(context, mockk())

        verify(exactly = 1) { anyConstructed<PreferenceApplier>().useNotificationWidget() }
    }

}