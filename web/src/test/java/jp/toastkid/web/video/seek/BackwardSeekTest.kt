package jp.toastkid.web.video.seek

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BackwardSeekTest {

    private lateinit var subject: BackwardSeek

    @Before
    fun setUp() {
        subject = BackwardSeek()
    }

    @Test
    fun makeScript() {
        val script = subject.makeScript(1)

        assertTrue(script.contains("var targetTime = endTime - 1"))
    }

    @Test
    fun isTarget() {
        assertFalse(subject.isTarget(null))
        assertFalse(subject.isTarget(""))
        assertFalse(subject.isTarget("https://www.yahoo.co.jp"))
        assertTrue(subject.isTarget("https://www.stockvoice.tv"))
    }

}