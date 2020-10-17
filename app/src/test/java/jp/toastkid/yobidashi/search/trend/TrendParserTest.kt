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
        assertEquals(
                20,
                TrendParser().invoke(Okio.buffer(Okio.source(javaClass.classLoader.getResourceAsStream("hot_trend/hourly.xml"))).readUtf8()).size
        )
    }

}