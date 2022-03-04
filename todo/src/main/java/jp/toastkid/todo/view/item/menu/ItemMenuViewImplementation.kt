/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.item.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import jp.toastkid.todo.R
import jp.toastkid.todo.databinding.PopupTodoTasksItemMenuBinding

class ItemMenuViewImplementation(private val context: Context) : ItemMenuPopupView {

    private val binding: PopupTodoTasksItemMenuBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        LAYOUT_ID,
        null,
        false
    )

    override fun setPopup(popup: ItemMenuPopup) {
        binding.popup = popup
    }

    override fun getView(): View {
        return binding.root
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.popup_todo_tasks_item_menu

    }

}