package jp.toastkid.yobidashi.rss.extractor

import okio.buffer
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.InputStream

/**
 * @author toastkidjp
 */
class RssUrlExtractorTest {

    private lateinit var extractor: RssUrlExtractor

    @Before
    fun setUp() {
        extractor = RssUrlExtractor()
    }

    @Test
    fun test() {
        val html = readStream("rss/extractor/sample.html").source().use { source ->
            source.buffer().use {
                it.readUtf8()
            }
        }

        val rssTargets = extractor.invoke(html)

        assertEquals(1, rssTargets?.size)
        assertEquals(
                "https://github.com/toastkidjp/Yobidashi_kt/commits/master.atom",
                rssTargets?.get(0)
        )
    }

    @Test
    fun testNull() {
        assertNull(extractor.invoke(null))
        assertNull(extractor.invoke(""))
        assertNull(extractor.invoke(" "))
    }

    @Test
    fun testIrregular() {
        val notContainsLink =
                """<link title="Recent Commits to Yobidashi_kt:master" type="application/atom+xml">"""
        assertTrue(extractor.invoke(notContainsLink)?.isEmpty() == true)
    }

    private fun readStream(filePath: String): InputStream =
            javaClass.classLoader?.getResourceAsStream(filePath)
                    ?: throw RuntimeException()
}