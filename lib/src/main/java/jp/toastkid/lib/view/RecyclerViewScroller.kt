package jp.toastkid.lib.view

import androidx.recyclerview.widget.RecyclerView

/**
 * @author toastkidjp
 */
object RecyclerViewScroller {

    private const val THRESHOLD = 30

    fun toTop(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount > THRESHOLD) {
            recyclerView.scrollToPosition(0)
            return
        }
        recyclerView.smoothScrollToPosition(0)
    }

    fun toBottom(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount > THRESHOLD) {
            recyclerView.scrollToPosition(itemCount - 1)
            return
        }
        recyclerView.smoothScrollToPosition(itemCount - 1)
    }
}