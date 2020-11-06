package jp.toastkid.yobidashi.search.trend

import okio.buffer
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class TrendParserTest {

    @Test
    fun test() {
        val resourceAsStream = javaClass.classLoader?.getResourceAsStream("hot_trend/hourly.xml")

        val xmlSource = resourceAsStream?.source()?.buffer()?.use { it.readUtf8() }
                ?: throw RuntimeException()

        val items = TrendParser().invoke(xmlSource)
        assertEquals(20, items.size)
        assertTrue(items.first().link.isNotBlank())
    }

}