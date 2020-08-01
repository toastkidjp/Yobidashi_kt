package jp.toastkid.article_viewer.zip

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class ZipFileChooserIntentFactoryTest {

    @Test
    fun test() {
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addCategory(any()) }.answers { mockk() }
        every { anyConstructed<Intent>().setType(any()) }.answers { mockk() }

        ZipFileChooserIntentFactory().invoke()

        verify(exactly = 1) { anyConstructed<Intent>().addCategory(Intent.CATEGORY_OPENABLE) }
        verify(exactly = 1) { anyConstructed<Intent>().setType("application/zip") }
    }

}