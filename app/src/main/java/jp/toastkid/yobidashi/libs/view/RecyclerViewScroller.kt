package jp.toastkid.yobidashi.libs.view

import android.support.v7.widget.RecyclerView

/**
 * @author toastkidjp
 */
object RecyclerViewScroller {

    fun toTop(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount > 30) {
            recyclerView.scrollToPosition(0);
            return
        }
        recyclerView.smoothScrollToPosition(0);
    }

    fun toBottom(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount > 30) {
            recyclerView.scrollToPosition(itemCount - 1)
            return
        }
        recyclerView.smoothScrollToPosition(itemCount)
    }
}