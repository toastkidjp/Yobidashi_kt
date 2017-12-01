package jp.toastkid.yobidashi.libs.view

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

/**
 * @author toastkidjp
 */
object RightSwipeActionAttacher {

    /**
     * For minimize usage.
     */
    private const val RIGHT = ItemTouchHelper.RIGHT

    fun invoke(recyclerView: RecyclerView, onSwipedAction: (Int) -> Unit) {
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(RIGHT, RIGHT) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean = true

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        if (direction != RIGHT) {
                            return
                        }
                        onSwipedAction(viewHolder.adapterPosition)
                    }
                }).attachToRecyclerView(recyclerView)

    }
}