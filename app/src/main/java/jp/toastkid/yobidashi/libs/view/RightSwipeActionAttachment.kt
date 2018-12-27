package jp.toastkid.yobidashi.libs.view

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

/**
 * Attach right bound swipe action.
 *
 * @author toastkidjp
 */
object RightSwipeActionAttachment {

    /**
     * For minimize usage.
     */
    private const val RIGHT = ItemTouchHelper.RIGHT

    /**
     * Attach action to passed [RecyclerView].
     *
     * @param recyclerView [RecyclerView]
     * @param onSwipedAction action.
     */
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