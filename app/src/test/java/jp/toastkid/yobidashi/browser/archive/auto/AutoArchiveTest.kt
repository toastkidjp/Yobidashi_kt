package jp.toastkid.yobidashi.browser.archive.auto

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.file.ExtensionRemover
import jp.toastkid.lib.storage.StorageWrapper
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * @author toastkidjp
 */
class AutoArchiveTest {

    @InjectMockKs
    private lateinit var autoArchive: AutoArchive

    @MockK
    private lateinit var filesDir: StorageWrapper

    @MockK
    private lateinit var extensionRemover: ExtensionRemover

    @MockK
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { file.getAbsolutePath() }.returns("test/test.mht")
        every { filesDir.assignNewFile(any<String>()) }.returns(file)
        every { filesDir.delete(any()) }.answers { Unit }
        every { filesDir.listFiles() }.returns(arrayOf(file))
        every { extensionRemover.invoke(any()) }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSave() {
        val webView = mockk<WebView>(relaxed = true)
        every { webView.saveWebArchive(any()) }.answers { Unit }

        autoArchive.save(webView, "test")

        verify(exactly = 1) { webView.saveWebArchive(any()) }
        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 1) { file.getAbsolutePath() }
    }

    @Test
    fun testDeleteWithNullTabId() {
        autoArchive.delete(null)

        verify(exactly = 0) { filesDir.delete(any()) }
    }

    // TODO Rename.
    @Test
    fun test() {
        assertFalse(AutoArchive.shouldNotUpdateTab("https://www.yahoo.co.jp/"))

        assertTrue(AutoArchive.shouldNotUpdateTab("file:///data/user/0/jp.toastkid.yobidashi.d" +
                "/files/auto_archives/search.yahoo.co.jp-realtime-search-" +
                "p%3D%E5%8F%8D%E5%AF%BE%201%E7%A5%A8%26ei%3DUTF-8.mht"))
    }
}