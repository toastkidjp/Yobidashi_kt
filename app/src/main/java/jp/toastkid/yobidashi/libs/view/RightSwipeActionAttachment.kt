package jp.toastkid.yobidashi.libs.view

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Attach right bound swipe action.
 *
 * @author toastkidjp
 */
class RightSwipeActionAttachment {

    /**
     * Attach action to passed [RecyclerView].
     *
     * @param recyclerView [RecyclerView]
     * @param onSwipedAction action.
     */
    operator fun invoke(recyclerView: RecyclerView, onSwipedAction: (Int) -> Unit) {
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

    companion object {

        /**
         * For minimize usage.
         */
        private const val RIGHT = ItemTouchHelper.RIGHT

    }
}