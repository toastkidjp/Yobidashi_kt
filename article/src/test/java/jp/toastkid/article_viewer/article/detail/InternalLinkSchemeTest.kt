package jp.toastkid.article_viewer.article.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class InternalLinkSchemeTest {

    @Test
    fun makeLink() {
        assertEquals(
                "internal-article://tomato",
                InternalLinkScheme.makeLink("tomato")
        )
    }

    @Test
    fun isInternalLink() {
        assertFalse(InternalLinkScheme.isInternalLink(""))
        assertFalse(InternalLinkScheme.isInternalLink(" "))
        assertFalse(InternalLinkScheme.isInternalLink("https://www.yahoo.co.jp"))
        assertFalse(InternalLinkScheme.isInternalLink("tomato"))
        assertTrue(InternalLinkScheme.isInternalLink(InternalLinkScheme.makeLink("tomato")))
    }

    @Test
    fun extract() {
        assertEquals(
                "",
                InternalLinkScheme.extract("")
        )
        assertEquals(
                " ",
                InternalLinkScheme.extract(" ")
        )
        assertEquals(
                "tomato",
                InternalLinkScheme.extract("tomato")
        )
        assertEquals(
                "https://www.yahoo.co.jp",
                InternalLinkScheme.extract("https://www.yahoo.co.jp")
        )
        assertEquals(
                "tomato",
                InternalLinkScheme.extract(InternalLinkScheme.makeLink("tomato"))
        )
    }
}