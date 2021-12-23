package jp.toastkid.image.list

import org.junit.Assert.assertSame
import org.junit.Test

/**
 * @author toastkidjp
 */
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