/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import jp.toastkid.todo.model.Board
import jp.toastkid.todo.model.Category
import jp.toastkid.todo.model.TodoTask
import jp.toastkid.todo.model.TodoTaskFts

/**
 * @author toastkidjp
 */
@Database(
        entities = [TodoTask::class, TodoTaskFts::class, Board::class, Category::class],
        exportSchema = false,
        version = 1
)
internal abstract class TodoTaskDatabase : RoomDatabase() {

    abstract fun repository(): TodoTaskDataAccessor

    companion object {

        private const val DB_NAME = "todo_task_db"

        fun find(activityContext: Context): TodoTaskDatabase {
            return Room.databaseBuilder(
                    activityContext.applicationContext,
                    TodoTaskDatabase::class.java,
                    DB_NAME
            )
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }
}