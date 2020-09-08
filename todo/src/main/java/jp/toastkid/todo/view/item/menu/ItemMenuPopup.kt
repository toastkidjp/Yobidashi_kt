/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.item.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import jp.toastkid.todo.R
import jp.toastkid.todo.databinding.PopupTodoTasksItemMenuBinding
import jp.toastkid.todo.model.TodoTask

/**
 *
 * @param context Use for obtaining [PopupWindow]
 * @param action [ItemMenuPopupActionUseCase]
 * @author toastkidjp
 */
class ItemMenuPopup(context: Context, private val action: ItemMenuPopupActionUseCase) {

    private val popupWindow = PopupWindow(context)

    private val binding: PopupTodoTasksItemMenuBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            LAYOUT_ID,
            null,
            false
    )

    private var targetId: Int? = null

    init {
        popupWindow.contentView = binding.root
        popupWindow.isOutsideTouchable = true
        popupWindow.width = context.resources.getDimensionPixelSize(R.dimen.item_menu_popup_width)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        binding.popup = this
    }

    fun show(view: View, item: TodoTask) {
        targetId = item.id
        popupWindow.showAsDropDown(view)
    }

    fun modify() {
        targetId?.let {
            action.modify(it)
        }
        popupWindow.dismiss()
    }

    fun delete() {
        targetId?.let {
            action.delete(it)
        }
        popupWindow.dismiss()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.popup_todo_tasks_item_menu

    }
}