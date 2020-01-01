package jp.toastkid.yobidashi.rss

import okio.Okio
import org.junit.Assert.*
import org.junit.Test
import java.io.InputStream

/**
 * TODO clean up code.
 *
 * @author toastkidjp
 */
class ParserTest {

    @Test
    fun test() {
        val rssText = Okio.buffer(Okio.source(readStream())).readUtf8()
        val rss = Parser().parse(rssText.split("\n"))
        assertNull(rss.creator)
        assertEquals("Sat, 21 Jan 2017 19:32:11 +0900", rss.date)
        assertEquals("This instance has only 1 item.", rss.description)
        assertEquals("http://www.yahoo.co.jp/", rss.link)
        assertEquals("RSS title", rss.title)
        assertNull(rss.url)
        assertTrue(rss.subjects.isEmpty())

        val items = rss.items
        assertFalse(items.isEmpty())

        val item = items.get(0)
        assertTrue(item.content.isEmpty())
        assertEquals("This item is a mere mock.", item.description)
        assertEquals("http://www.yahoo.co.jp", item.link)
        assertEquals("Item title", item.title)
    }

    @Test
    fun testAtom() {
        val rssText = Okio.buffer(Okio.source(readStream())).readUtf8()

        val rss = Parser().parse(rssText.split("\n"))
        assertEquals("Private Feed for toastkidjp", rss.title)
        assertEquals(30, rss.items.size)

        val item = rss.items[0]
        assertEquals("MWGuy starred Turaiiao/crush", item.title)
        assertEquals("https://github.com/Turaiiao/crush", item.link)
    }

    @Test
    fun testRdf() {
        val rssText = Okio.buffer(Okio.source(readStream())).readUtf8()

        val rss = Parser().parse(rssText.split("\n"))
        assertEquals("なんJ（まとめては）いかんのか？", rss.title)
        assertEquals(3, rss.items.size)

        val item = rss.items[0]
        assertEquals("ヤクルト・塩見、ダーツを破壊ｗｗｗｗ", item.title)
        assertEquals("http://blog.livedoor.jp/livejupiter2/archives/9541826.html", item.link)
        println(item.content)
    }

    private fun readStream(): InputStream =
            javaClass.classLoader?.getResourceAsStream("rss/sample.xml")
                    ?: throw RuntimeException()
}