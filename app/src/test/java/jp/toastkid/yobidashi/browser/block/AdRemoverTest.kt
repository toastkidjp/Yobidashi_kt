package jp.toastkid.yobidashi.browser.block

import okio.Okio
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Check of [AdRemover]'s behavior.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class AdRemoverTest {

    /**
     * Test target.
     */
    private var adRemover = AdRemover(javaClass.classLoader.getResourceAsStream("ad/ad_hosts.txt"))

    @Test
    fun test() {
        val response = adRemover(
                "https://im.ov.yahoo.co.jp/tag/" +
                        "?adprodset=58335_241697-265175-289342" +
                        "&noad_cb=noad_callback_func&vimps_mode=3"
        )

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        Okio.buffer(Okio.source(response?.data)).use {
            assertTrue(it.readUtf8().isEmpty())
        }
    }
}