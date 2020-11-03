/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.board

import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class SampleTaskMaker {

    operator fun invoke(): TodoTask {
        val currentTimeMillis = System.currentTimeMillis()

        val sampleTask = TodoTask(0)
        sampleTask.dueDate = currentTimeMillis
        sampleTask.lastModified = currentTimeMillis
        sampleTask.created = currentTimeMillis
        sampleTask.description = "Sample task"
        sampleTask.x = 200f
        sampleTask.y = 200f
        return sampleTask
    }

}