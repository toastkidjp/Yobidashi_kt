package jp.toastkid.lib.file

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ExtensionRemoverTest {

    private lateinit var extensionRemover: ExtensionRemover

    @Before
    fun setUp() {
        extensionRemover = ExtensionRemover()
    }

    @Test
    fun test() {
        assertEquals("", extensionRemover(""))
        assertEquals("test", extensionRemover("test"))
        assertEquals("test", extensionRemover("test."))
        assertEquals("test", extensionRemover("test.txt"))
        assertEquals("test.exe", extensionRemover("test.exe.txt"))
    }

}