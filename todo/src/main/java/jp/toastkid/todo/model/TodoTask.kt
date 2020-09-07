/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.model

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author toastkidjp
 */
@Entity
class TodoTask(
        @PrimaryKey(autoGenerate = true)
        var id: Int
) {
    var description: String = ""

    var bigram: String = ""

    var created: Long = 0L

    var lastModified: Long = 0L

    var dueDate: Long = 0L

    var categoryId: Int = 0

    var boardId: Int = 0

    var x: Int = 0

    var y: Int = 0

    @ColorInt
    var color: Int = Color.CYAN

    var done: Boolean = false
}