/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author toastkidjp
 */
@Entity
class Category(
        @PrimaryKey(autoGenerate = true)
        var id: Int
) {

    var name: String = ""

    var description: String = ""

    var created: Long = 0L

    var modified: Long = 0L

}