package jp.toastkid.article_viewer.tokenizer

import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * @author toastkidjp
 */
class NgramTokenizerTest {

    @Test
    fun test() {
        val tokenizer = NgramTokenizer()
        assertEquals("東 京 特 許 許 可 局", tokenizer("東京特許許可局", 1))
        assertEquals("東京 京特 特許 許許 許可 可局", tokenizer("東京特許許可局", 2))
        assertEquals("東京特 京特許 特許許 許許可 許可局", tokenizer("東京特許許可局", 3))
    }

    @Ignore
    fun bench() {
        val tokenizer = NgramTokenizer()
        println(measureTimeMillis {
            repeat(5000) {
                tokenizer(
                    "東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局東京特許許可局",
                    2
                )
            }
        })
    }

}