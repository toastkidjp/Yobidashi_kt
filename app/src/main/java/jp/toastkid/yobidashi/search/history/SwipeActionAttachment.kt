package jp.toastkid.yobidashi.search.history

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.view.SwipeViewHolder

/**
 * @author toastkidjp
 */
class SwipeActionAttachment {

    operator fun invoke(recyclerView: RecyclerView) {
        ItemTouchHelper(makeLeft(recyclerView)).attachToRecyclerView(recyclerView)
        ItemTouchHelper(makeRight()).attachToRecyclerView(recyclerView)
    }

    private fun makeLeft(recyclerView: RecyclerView): ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun getSwipeEscapeVelocity(defaultValue: Float) = 5_000f

                override fun onMove(
                        rv: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                ) = true

                override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int
                ) {
                    val holder = viewHolder as? SwipeViewHolder?
                    if (holder?.isButtonVisible() == false) {
                        return
                    }
                    val adapter = recyclerView.adapter
                    if (adapter is Removable) {
                        adapter.removeAt(viewHolder.adapterPosition)
                        holder?.hideButton()
                    }
                }

                override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    getDefaultUIUtil().clearView((viewHolder as? SwipeViewHolder?)?.getFrontView())
                }

                override fun onChildDraw(
                        c: Canvas,
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        dX: Float,
                        dY: Float,
                        actionState: Int,
                        isCurrentlyActive: Boolean
                ) {
                    if (viewHolder.adapterPosition == -1) {
                        return
                    }

                    val holder = viewHolder as? SwipeViewHolder?
                    if (holder?.isButtonVisible() == false) {
                        holder.showButton()
                    }
                    getDefaultUIUtil().onDraw(
                            c,
                            recyclerView,
                            holder?.getFrontView(),
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                    )
                }
            }

    private fun makeRight(): ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                override fun getMovementFlags(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val holder = viewHolder as? SwipeViewHolder?
                    if (holder?.isButtonVisible() == false) {
                        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, 0)
                    }
                    return super.getMovementFlags(recyclerView, viewHolder)
                }

                override fun onMove(
                        rv: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

                override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                ) {
                    getDefaultUIUtil().clearView((viewHolder as? SwipeViewHolder)?.getFrontView())
                }

                override fun onChildDraw(
                        c: Canvas,
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        dX: Float,
                        dY: Float,
                        actionState: Int,
                        isCurrentlyActive: Boolean
                ) {
                    val holder = viewHolder as? SwipeViewHolder?
                    if (holder?.isButtonVisible() == true) {
                        holder.hideButton()
                    }
                    getDefaultUIUtil().onDraw(
                            c,
                            recyclerView,
                            holder?.getFrontView(),
                            dX,
                            dY,
                            actionState, isCurrentlyActive
                    )
                }
            }

}