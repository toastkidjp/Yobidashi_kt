package jp.toastkid.yobidashi.browser.block

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
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
    private lateinit var adRemover: AdRemover

    @Before
    fun setUp() {
        val inputStream = javaClass.classLoader?.getResourceAsStream("ad/ad_hosts.txt") ?: return fail()
        adRemover = AdRemover(inputStream)
    }

    @Test
    fun test() {
        val response = adRemover(
                "https://im.ov.yahoo.co.jp/tag/" +
                        "?adprodset=58335_241697-265175-289342" +
                        "&noad_cb=noad_callback_func&vimps_mode=3"
        )

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        response?.data?.bufferedReader()?.use {
            assertTrue(it.readLine().isEmpty())
        }
    }
}