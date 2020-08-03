package jp.toastkid.yobidashi.main

import android.content.res.Configuration
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * @author toastkidjp
 */
class LocaleWrapperTest {

    @MockK
    private lateinit var configuration: Configuration

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testIsJapanese() {
        configuration.locale = Locale.JAPANESE

        assertTrue(LocaleWrapper.isJa(configuration))
    }

    @Test
    fun testIsElse() {
        configuration.locale = Locale.ENGLISH

        assertFalse(LocaleWrapper.isJa(configuration))
    }

}