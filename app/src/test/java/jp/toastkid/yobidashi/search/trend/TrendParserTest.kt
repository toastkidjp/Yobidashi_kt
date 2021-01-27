package jp.toastkid.yobidashi.search.trend

import okio.Okio
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class TrendParserTest {

    @Test
    fun test() {
        val resourceAsStream = javaClass.classLoader.getResourceAsStream("hot_trend/hourly.xml")
        val xmlSource = Okio.buffer(Okio.source(resourceAsStream)).readUtf8()
        val items = TrendParser().invoke(xmlSource)
        assertEquals(20, items.size)
    }

}