package jp.toastkid.yobidashi.media.image

import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class SortTest {

    @Test
    fun test() {
        assertSame(Sort.default(), Sort.findByName(null))
        assertSame(Sort.default(), Sort.findByName(""))
        assertSame(Sort.default(), Sort.findByName(" "))
        assertSame(Sort.DATE, Sort.findByName("DATE"))
        assertSame(Sort.default(), Sort.findByName("date"))
    }
}