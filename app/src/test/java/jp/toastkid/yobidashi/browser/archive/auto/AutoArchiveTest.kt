package jp.toastkid.yobidashi.browser.archive.auto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class AutoArchiveTest {

    @Test
    fun test() {
        assertFalse(AutoArchive.shouldNotUpdateTab("https://www.yahoo.co.jp/"))

        assertTrue(AutoArchive.shouldNotUpdateTab("file:///data/user/0/jp.toastkid.yobidashi.d" +
                "/files/auto_archives/search.yahoo.co.jp-realtime-search-" +
                "p%3D%E5%8F%8D%E5%AF%BE%201%E7%A5%A8%26ei%3DUTF-8.mht"))
    }
}