package jp.toastkid.yobidashi.wikipedia.today

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * @author toastkidjp
 */
class DateArticleUrlFactoryTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var configuration: Configuration

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { resources.configuration }.answers { configuration }
        every { context.resources }.answers { resources }
    }

    @Test
    fun testIrregularCase() {
        configuration.locale = Locale.JAPANESE
        every { context.getString(any()) }.answers { "https://ja.wikipedia.org/wiki/{0}月{1}日" }

        assertEquals(
                "",
                DateArticleUrlFactory().invoke(context, -1, 1)
        )

        assertEquals(
                "",
                DateArticleUrlFactory().invoke(context, 12, 1)
        )

        assertEquals(
                "",
                DateArticleUrlFactory().invoke(context, 1, 0)
        )

        assertEquals(
                "",
                DateArticleUrlFactory().invoke(context, 1, 32)
        )
    }

    @Test
    fun testJapanese() {
        configuration.locale = Locale.JAPANESE
        every { context.getString(any()) }.answers { "https://ja.wikipedia.org/wiki/{0}月{1}日" }

        assertEquals(
                "https://ja.wikipedia.org/wiki/1月1日",
                DateArticleUrlFactory().invoke(context, 0, 1)
        )
    }

    @Test
    fun testEnglish() {
        configuration.locale = Locale.ENGLISH
        every { context.getString(any()) }.answers { "https://en.wikipedia.org/wiki/{0}_{1}" }

        assertEquals(
                "https://en.wikipedia.org/wiki/January_1",
                DateArticleUrlFactory().invoke(context, 0, 1)
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}