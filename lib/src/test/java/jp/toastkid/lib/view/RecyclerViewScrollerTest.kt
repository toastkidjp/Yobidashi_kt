package jp.toastkid.lib.view

import androidx.recyclerview.widget.RecyclerView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class RecyclerViewScrollerTest {

    @MockK
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { recyclerView.scrollToPosition(any()) }.answers { Unit }
        every { recyclerView.smoothScrollToPosition(any()) }.answers { Unit }
    }

    @Test
    fun testToTopWith1() {
        RecyclerViewScroller.toTop(recyclerView, 1)

        verify(atLeast = 1) { recyclerView.smoothScrollToPosition(0) }
        verify(exactly = 0) { recyclerView.scrollToPosition(any()) }
    }

    @Test
    fun testToTopWith31() {
        RecyclerViewScroller.toTop(recyclerView, 31)

        verify(atLeast = 1) { recyclerView.scrollToPosition(0) }
        verify(exactly = 0) { recyclerView.smoothScrollToPosition(any()) }
    }


    @Test
    fun testToBottomWith1() {
        RecyclerViewScroller.toBottom(recyclerView, 1)

        verify(atLeast = 1) { recyclerView.smoothScrollToPosition(0) }
        verify(exactly = 0) { recyclerView.scrollToPosition(any()) }
    }

    @Test
    fun testToBottomWith31() {
        RecyclerViewScroller.toBottom(recyclerView, 31)

        verify(atLeast = 1) { recyclerView.scrollToPosition(30) }
        verify(exactly = 0) { recyclerView.smoothScrollToPosition(any()) }
    }

}