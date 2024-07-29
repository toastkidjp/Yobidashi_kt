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

    fun show() {
        _bottomSheetScaffoldState.value = true
    }

    fun hide() {
        _bottomSheetScaffoldState.value = false
    }

}