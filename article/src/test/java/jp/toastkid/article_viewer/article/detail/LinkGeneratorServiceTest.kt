package jp.toastkid.article_viewer.article.detail

import android.text.util.Linkify
import android.widget.TextView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LinkGeneratorServiceTest {

    @MockK
    private lateinit var textView: TextView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        mockkStatic(Linkify::class)
        every { Linkify.addLinks(textView, any(), null, null, any()) }.answers { Unit }

        LinkGeneratorService().invoke(textView)

        verify(exactly = 2) { Linkify.addLinks(textView, any(), null, null, any()) }
    }

}