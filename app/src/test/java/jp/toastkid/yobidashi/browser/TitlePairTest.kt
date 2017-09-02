package jp.toastkid.yobidashi.browser

import junit.framework.Assert
import org.junit.Test

/**
 * [TitlePair]'s test cases.
 *
 * @author toastkidjp
 */
class TitlePairTest {

    @Test
    fun test() {
        val title = "title"
        val sub = "subtitle"
        val titlePair = TitlePair.make(title, sub);

        Assert.assertEquals(title, titlePair.title())
        Assert.assertEquals(sub, titlePair.subtitle())
    }
}