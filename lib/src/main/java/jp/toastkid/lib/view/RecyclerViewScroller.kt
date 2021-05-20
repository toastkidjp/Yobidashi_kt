package jp.toastkid.lib.view

import androidx.recyclerview.widget.RecyclerView

/**
 * @author toastkidjp
 */
object RecyclerViewScroller {

    private const val THRESHOLD = 30

    private val TOP_POSITION = 0

    fun toTop(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount > THRESHOLD) {
            recyclerView.scrollToPosition(TOP_POSITION)
            return
        }
        recyclerView.smoothScrollToPosition(TOP_POSITION)
    }

    fun toBottom(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount > THRESHOLD) {
            recyclerView.scrollToPosition(itemCount - 1)
            return
        }
        recyclerView.smoothScrollToPosition(itemCount - 1)
    }
}