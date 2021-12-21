package jp.toastkid.media.image.list

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class ExcludingItemFilterTest {

    @Test
    fun test() {
        val excludingItemFilter = ExcludingItemFilter(setOf("tomato", "onion"))
        assertTrue(excludingItemFilter(null))
        assertTrue(excludingItemFilter(""))
        assertTrue(excludingItemFilter("  "))
        assertFalse(excludingItemFilter("tomato"))
        assertFalse(excludingItemFilter("onion"))
        assertTrue(excludingItemFilter("toast"))
    }

}