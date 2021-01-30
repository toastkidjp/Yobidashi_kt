package jp.toastkid.article_viewer.zip

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ZipFileChooserIntentFactoryTest {

    private lateinit var zipFileChooserIntentFactory: ZipFileChooserIntentFactory

    @Before
    fun setUp() {
        zipFileChooserIntentFactory = ZipFileChooserIntentFactory()
    }

    @Test
    fun test() {
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addCategory(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().setType(any()) }.answers { mockk() }

        zipFileChooserIntentFactory.invoke()

        verify(exactly = 1) { anyConstructed<Intent>().addCategory(Intent.CATEGORY_OPENABLE) }
        verify(exactly = 1) { anyConstructed<Intent>().setType("application/zip") }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}