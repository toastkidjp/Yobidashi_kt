/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.item.menu

import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class ItemMenuPopupActionUseCase(
        private val modifyAction: (TodoTask) -> Unit,
        private val deleteAction: (TodoTask) -> Unit
) {

    fun modify(task: TodoTask) {
        modifyAction(task)
    }

    fun delete(task: TodoTask) {
        deleteAction(task)
    }
}