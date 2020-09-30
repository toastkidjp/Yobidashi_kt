/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.board

import android.view.ViewGroup
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class TaskAddingUseCase(
        private val color: Int,
        private val tasks: MutableList<TodoTask>,
        private val board: ViewGroup,
        private val boardItemViewFactory: BoardItemViewFactory
) {

    operator fun invoke(task: TodoTask) {
        tasks.add(task)

        val itemView = boardItemViewFactory.invoke(board, task, color)

        itemView.tag = task.id
        board.addView(itemView)
    }

}