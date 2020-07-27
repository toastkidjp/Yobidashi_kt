package jp.toastkid.yobidashi.tab.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class ArticleTabTest {

    @Test
    fun testMake() {
        val articleTab = ArticleTab.make("title")
        assertEquals("title", articleTab.title())
    }

}