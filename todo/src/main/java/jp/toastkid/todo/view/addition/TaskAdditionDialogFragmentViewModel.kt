/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.addition

import androidx.compose.runtime.mutableStateOf
import jp.toastkid.todo.model.TodoTask
import java.io.Serializable

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragmentViewModel : Serializable {

    private val _bottomSheetScaffoldState = mutableStateOf(false)

    fun bottomSheetScaffoldState() = _bottomSheetScaffoldState.value

    private val _task = mutableStateOf<TodoTask?>(null)

    fun task() = _task.value

    fun setTask(task: TodoTask?) {
        _task.value = task ?: TodoTask(0).also { it.dueDate = System.currentTimeMillis() }
    }

    fun save(
        task: TodoTask?,
        onTapAdd: (TodoTask) -> Unit
    ) {
        task?.let {
            updateTask(it)
            onTapAdd(it)
        }
    }

    private fun updateTask(task: TodoTask?) {
        if (task?.created == 0L) {
            task.created = System.currentTimeMillis()
        }
        task?.lastModified = System.currentTimeMillis()
    }

    fun colors() = colors

    fun show() {
        _bottomSheetScaffoldState.value = true
    }

    fun hide() {
        _bottomSheetScaffoldState.value = false
    }

}

private val colors = setOf(
    0xffe53935, 0xfff8bbd0, 0xff2196f3, 0xff4caf50, 0xffffeb3b, 0xff3e2723, 0xffffffff
)