/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.list.initial

import android.graphics.Color
import androidx.annotation.WorkerThread
import jp.toastkid.todo.data.TodoTaskDataAccessor
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class InitialTaskPreparation(private val repository: TodoTaskDataAccessor) {

    /**
     * <pre>
     * CoroutineScope(Dispatchers.IO).launch {
     *     InitialTaskPreparation(repository).invoke()
     * }
     * </pre>
     */
    @WorkerThread
    operator fun invoke() {
        insertTask("Tasks will be added here.", Color.RED)
        insertTask("Please would you add by press below 'Add' button.", Color.BLUE)
    }

    private fun insertTask(description: String, color: Int) {
        repository.insert(
                TodoTask(0).also {
                    it.color = color
                    it.description = description
                }
        )
    }
}