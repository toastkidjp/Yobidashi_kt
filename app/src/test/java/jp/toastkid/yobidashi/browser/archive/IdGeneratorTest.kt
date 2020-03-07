package jp.toastkid.yobidashi.browser.archive

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class IdGeneratorTest {

    @Test
    fun test() {
        val idGenerator = IdGenerator()
        assertNull(idGenerator.from(null))
        assertTrue(idGenerator.from("")?.isEmpty() ?: false)
        assertEquals(
                "search.yahoo.co.jp--",
                idGenerator.from("https://search.yahoo.co.jp/")
        )
        assertEquals(
                "search.yahoo.co.jp--p=orange",
                idGenerator.from("https://search.yahoo.co.jp/?p=orange")
        )
        assertEquals(
                "www.yahoo.co.jp-search-p=tomato",
                idGenerator.from("https://www.yahoo.co.jp/search?p=tomato")
        )
    }
}