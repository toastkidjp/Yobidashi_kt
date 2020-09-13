/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.item.menu

import jp.toastkid.todo.data.TodoTaskDataAccessor
import jp.toastkid.todo.model.TodoTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ItemMenuPopupActionUseCase(
        private val repository: TodoTaskDataAccessor,
        private val modifyAction: (TodoTask) -> Unit,
        private val refresh: () -> Unit
) {

    fun modify(task: TodoTask) {
        CoroutineScope(Dispatchers.Main).launch {
            modifyAction(task)
        }
    }

    fun delete(task: TodoTask) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.delete(task)
            }
            refresh()
        }
    }
}