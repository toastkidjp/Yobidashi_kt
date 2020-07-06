package jp.toastkid.yobidashi.libs

import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ThumbnailGeneratorTest {

    private lateinit var thumbnailGenerator: ThumbnailGenerator

    @MockK
    private lateinit var view: View

    @Before
    fun setUp() {
        thumbnailGenerator = ThumbnailGenerator()
        MockKAnnotations.init(this)
    }

    @Test
    fun testNull() {
        thumbnailGenerator.invoke(null)
    }

    @Test
    fun test() {
        every { view.getMeasuredWidth() }.answers { 0 }
        every { view.getMeasuredHeight() }.answers { 0 }
        every { view.invalidate() }.answers { Unit }
        every { view.draw(any()) }.answers { Unit }

        thumbnailGenerator.invoke(view)

        verify(exactly = 0) { view.invalidate() }
        verify(exactly = 0) { view.draw(any()) }
    }

    @Test
    fun test1() {
        every { view.getMeasuredWidth() }.answers { 1 }
        every { view.getMeasuredHeight() }.answers { 1 }
        every { view.invalidate() }.answers { Unit }
        every { view.draw(any()) }.answers { Unit }

        thumbnailGenerator.invoke(view)

        verify(exactly = 1) { view.invalidate() }
        verify(exactly = 1) { view.draw(any()) }
    }

}