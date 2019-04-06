/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab.tab_list

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper

/**
 * @author toastkidjp
 */
object DragAttachment {

    operator fun invoke(recyclerView: RecyclerView, direction: Int) {
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(direction, 0) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        val adapter = recyclerView.adapter
                        if (adapter is Adapter) {
                            adapter.swap(viewHolder.adapterPosition, target.adapterPosition)
                        }
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) = Unit
                }
        ).attachToRecyclerView(recyclerView)
    }
}