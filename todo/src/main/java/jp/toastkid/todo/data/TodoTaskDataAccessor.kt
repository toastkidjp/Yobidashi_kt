/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
@Dao
interface TodoTaskDataAccessor {

    @Query("SELECT * FROM todotask")
    fun allTasks(): PagingSource<Int, TodoTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: TodoTask)

    @Delete
    fun delete(task: TodoTask)

}