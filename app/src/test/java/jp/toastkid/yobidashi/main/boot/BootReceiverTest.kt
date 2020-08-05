package jp.toastkid.yobidashi.main.boot

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
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

        mockkObject(NotificationWidget)
        every { NotificationWidget.show(any()) }.answers { Unit }

        mockkConstructor(PreferenceApplier::class)
    }

    @Test
    fun testPreferenceTrue() {
        every { anyConstructed<PreferenceApplier>().useNotificationWidget() }.answers { true }

        BootReceiver().onReceive(context, mockk())

        verify(exactly = 1) { anyConstructed<PreferenceApplier>().useNotificationWidget() }
        verify(exactly = 1) { NotificationWidget.show(any()) }
    }

    @Test
    fun testPreferenceFalse() {
        every { anyConstructed<PreferenceApplier>().useNotificationWidget() }.answers { false }

        BootReceiver().onReceive(context, mockk())

        verify(exactly = 1) { anyConstructed<PreferenceApplier>().useNotificationWidget() }
        verify(exactly = 0) { NotificationWidget.show(any()) }
    }

}