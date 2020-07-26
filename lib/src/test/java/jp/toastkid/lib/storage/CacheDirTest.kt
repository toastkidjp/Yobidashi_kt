package jp.toastkid.lib.storage

import jp.toastkid.lib.storage.CacheDir
import jp.toastkid.lib.storage.FilesDir
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
class CacheDirTest {

    /** Test object.  */
    private var cacheDir: CacheDir? = null

    /**
     * Initialize test object before each test method.
     */
    @Before
    fun setUp() {
        cacheDir = CacheDir(RuntimeEnvironment.application, "test")
    }

    /**
     * Check [FilesDir.assignNewFile].
     */
    @Test
    fun test_assignNewFile() {
        assertEquals(
                "あま_しお_ちゃ_ん.html",
                cacheDir?.assignNewFile("あま/しお\\ちゃ|ん.html")?.name
        )
    }
}
