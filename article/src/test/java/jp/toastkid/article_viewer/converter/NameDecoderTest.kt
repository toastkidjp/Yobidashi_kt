package jp.toastkid.article_viewer.converter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class NameDecoderTest {

    @Test
    operator fun invoke() {
        val path = "article/C6FCB5AD323031392D30342D333028B2D029.md"
        assertEquals(
            "日記2019-04-30(火)",
            NameDecoder(path.substring(path.indexOf("/") + 1, path.lastIndexOf(".")))
        )
    }
}