package jp.toastkid.article.calendar

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class TitleFilterGeneratorTest {

    @Test
    fun test() {
        assertEquals(
            "2019-01-01%",
            TitleFilterGenerator(2019, 1, 1)
        )
        assertEquals(
            "2019-10-10%",
            TitleFilterGenerator(2019, 10, 10)
        )
        assertEquals(
            "2019-09-09%",
            TitleFilterGenerator(2019, 9, 9)
        )
        assertEquals(
            "2019-11-11%",
            TitleFilterGenerator(2019, 11, 11)
        )
    }
}