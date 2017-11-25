package jp.toastkid.yobidashi.browser.bookmark

import com.squareup.moshi.Moshi
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import okio.Okio
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class ExporterTest {

    private val filePath = "bookmark/bookmarkJsons.txt"

    @Test
    fun test_invoke() {
        val adapter = Moshi.Builder().build().adapter(Bookmark::class.java)
        val message = Exporter(readFile().map { adapter.fromJson(it) }.toList()).invoke()
        println(message)
    }

    private fun readFile() =
            Okio.buffer(Okio.source(ExporterTest::class.java.classLoader.getResourceAsStream(filePath)))
                    .readUtf8().split("\n")

}