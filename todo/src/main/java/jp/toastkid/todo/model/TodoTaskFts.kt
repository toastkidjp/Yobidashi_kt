/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/**
 * @author toastkidjp
 */
@Fts4(contentEntity = TodoTask::class, tokenizer = FtsOptions.TOKENIZER_PORTER)
@Entity(tableName = "todoTaskFts")
class TodoTaskFts {
    @Keep
    var bigram: String = ""
}