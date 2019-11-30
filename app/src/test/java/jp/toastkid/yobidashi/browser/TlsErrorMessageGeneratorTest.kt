package jp.toastkid.yobidashi.browser

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class TlsErrorMessageGeneratorTest {

    private val tlsErrorMessageGenerator = TlsErrorMessageGenerator()

    @Test
    fun testNullCase() {
        assertTrue(
                tlsErrorMessageGenerator.invoke(RuntimeEnvironment.systemContext, null).isEmpty()
        )
    }
}