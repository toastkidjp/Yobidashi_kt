package jp.toastkid.article_viewer.calendar

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TitleFilterGeneratorTest {

    private lateinit var titleFilterFenerator: TitleFilterGenerator

    @Before
    fun setUp() {
        titleFilterFenerator = TitleFilterGenerator()
    }

    @Test
    fun test() {
        assertEquals(
                "2019-01-01%",
                titleFilterFenerator(2019, 1, 1)
        )
        assertEquals(
                "2019-10-10%",
                titleFilterFenerator(2019, 10, 10)
        )
        assertEquals(
                "2019-09-09%",
                titleFilterFenerator(2019, 9, 9)
        )
        assertEquals(
                "2019-11-11%",
                titleFilterFenerator(2019, 11, 11)
        )
    }
}