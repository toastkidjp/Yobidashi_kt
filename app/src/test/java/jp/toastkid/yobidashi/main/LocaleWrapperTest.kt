package jp.toastkid.yobidashi.main

import android.content.res.Configuration
import android.os.LocaleList
import io.mockk.MockKAnnotations
import io.mockk.every
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

    @MockK
    private lateinit var localeList: LocaleList

    private lateinit var localeWrapper: LocaleWrapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        localeWrapper = LocaleWrapper()

        every { configuration.locales }.returns(localeList)
        every { localeList.isEmpty }.returns(false)
        every { localeList.get(any()) }.returns(Locale.JAPANESE)
    }

    @Test
    fun testIsJapanese() {
        assertTrue(localeWrapper.isJa(configuration))
    }

    @Test
    fun testIsElse() {
        every { localeList.get(any()) }.returns(Locale.ENGLISH)

        assertFalse(localeWrapper.isJa(configuration))
    }

}