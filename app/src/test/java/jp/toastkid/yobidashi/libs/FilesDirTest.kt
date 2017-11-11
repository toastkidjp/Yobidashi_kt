package jp.toastkid.yobidashi.libs

import jp.toastkid.yobidashi.libs.storage.FilesDir
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * [FilesDir]'s test case.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class FilesDirTest {

    /**
     * Test object.
     */
    private var filesDir: FilesDir? = null

    /**
     * Initialize test object before each test method.
     */
    @Before
    fun setUp() {
        filesDir = FilesDir(RuntimeEnvironment.application, "test")
    }

    /**
     * Check [FilesDir.assignNewFile].
     */
    @Test
    fun test_assignNewFile() {
        assertEquals(
                "あま_しお_ちゃ_ん.html",
                filesDir!!.assignNewFile("あま/しお\\ちゃ|ん.html").name
        )
    }
}
