package jp.toastkid.yobidashi.rss

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jp.toastkid.yobidashi.rss.model.Parser
import jp.toastkid.yobidashi.rss.model.Rss
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class RssResponseConverterTest {

    @MockK
    private lateinit var parser: Parser

    @MockK
    private lateinit var responseBody: ResponseBody

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { parser.parse(any()) }.answers { Rss() }

        val converter = RssResponseConverter(parser)
        every { responseBody.string() }.answers { "" }

        converter.convert(responseBody)

        verify(exactly = 1) { parser.parse(any()) }
        verify(exactly = 1) { responseBody.string() }
    }
}