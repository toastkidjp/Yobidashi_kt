package jp.toastkid.yobidashi.browser.bookmark

import com.squareup.moshi.Moshi
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import okio.buffer
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test of [Exporter].
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class ExporterTest {

    /**
     * Use for read test resources.
     */
    private val classLoader = ExporterTest::class.java.classLoader

    /**
     * Source file path.
     */
    private val sourcePath: String = "bookmark/bookmarkJsons.txt"

    /**
     * Expected file path.
     */
    private val expectedPath: String = "bookmark/expectedExported.html"

    /**
     * Check of [Exporter.invoke].
     */
    @Test
    fun test_invoke() {
        val adapter = Moshi.Builder().build().adapter(Bookmark::class.java)
        val message = Exporter(readSource()?.map { adapter.fromJson(it) }?.toList()!!).invoke()
        assertEquals(readExpected(), message.replace("\n", ""))
    }

    /**
     * Read [Bookmark] objects from source.
     */
    private fun readSource(): List<String>? =
            classLoader?.getResourceAsStream(sourcePath)?.source()?.use { source ->
                source.buffer().use { bufferedSource ->
                    val sourceText: String? = bufferedSource.readUtf8()
                    bufferedSource.close()
                    return sourceText?.split("\n")
                }
            }

    /**
     * Read expected html string.
     */
    private fun readExpected() =
            classLoader?.getResourceAsStream(expectedPath)?.source()?.use { source ->
                source.buffer().use {
                    it.readUtf8().replace("\r\n", "")
                }
            }

}