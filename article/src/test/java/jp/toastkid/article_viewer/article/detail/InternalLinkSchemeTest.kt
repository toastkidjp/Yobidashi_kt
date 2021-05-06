package jp.toastkid.article_viewer.article.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class InternalLinkSchemeTest {

    private lateinit var internalLinkScheme: InternalLinkScheme

    @Before
    fun setUp() {
        internalLinkScheme = InternalLinkScheme()
    }

    @Test
    fun makeLink() {
        assertEquals(
                "internal-article://tomato",
                internalLinkScheme.makeLink("tomato")
        )
    }

    @Test
    fun isInternalLink() {
        assertFalse(internalLinkScheme.isInternalLink(""))
        assertFalse(internalLinkScheme.isInternalLink(" "))
        assertFalse(internalLinkScheme.isInternalLink("https://www.yahoo.co.jp"))
        assertFalse(internalLinkScheme.isInternalLink("tomato"))
        assertTrue(internalLinkScheme.isInternalLink(internalLinkScheme.makeLink("tomato")))
    }

    @Test
    fun extract() {
        assertEquals(
                "",
                internalLinkScheme.extract("")
        )
        assertEquals(
                " ",
                internalLinkScheme.extract(" ")
        )
        assertEquals(
                "tomato",
                internalLinkScheme.extract("tomato")
        )
        assertEquals(
                "https://www.yahoo.co.jp",
                internalLinkScheme.extract("https://www.yahoo.co.jp")
        )
        assertEquals(
                "tomato",
                internalLinkScheme.extract(internalLinkScheme.makeLink("tomato"))
        )
    }
}