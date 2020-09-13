/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.toastkid.lib.view.DraggableTouchListener
import jp.toastkid.todo.R
import jp.toastkid.todo.databinding.ItemTaskShortBinding
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class BoardItemViewFactory(private val layoutInflater: LayoutInflater) {

    operator fun invoke(parent: ViewGroup?, it: TodoTask, menuColor: Int): View {
        val itemBinding = DataBindingUtil.inflate<ItemTaskShortBinding>(
                layoutInflater,
                R.layout.item_task_short,
                parent,
                false
        )

        itemBinding.color.setBackgroundColor(it.color)
        itemBinding.mainText.text = it.description
        itemBinding.menu.setColorFilter(menuColor)

        val draggableTouchListener = DraggableTouchListener()
        draggableTouchListener.setCallback(object : DraggableTouchListener.OnNewPosition {

            override fun onNewPosition(x: Float, y: Float) {
                it.x = x
                it.y = y
            }

        })

        itemBinding.root.setOnTouchListener(draggableTouchListener)

        return itemBinding.root
    }
}