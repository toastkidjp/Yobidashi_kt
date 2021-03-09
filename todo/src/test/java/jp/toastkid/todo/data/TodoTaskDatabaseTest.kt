/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class TodoTaskDatabaseTest {

    @MockK
    private lateinit var databaseBuilder: RoomDatabase.Builder<TodoTaskDatabase>

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Room::class)
        every { Room.databaseBuilder(any(), any<Class<TodoTaskDatabase>>(), any()) }.returns(databaseBuilder)

        every { databaseBuilder.fallbackToDestructiveMigration() }.returns(databaseBuilder)
        every { databaseBuilder.build() }.returns(mockk())

        every { context.getApplicationContext() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        TodoTaskDatabase.find(context)

        verify(exactly = 1) { Room.databaseBuilder(any(), any<Class<TodoTaskDatabase>>(), any()) }
        verify(exactly = 1) { databaseBuilder.fallbackToDestructiveMigration() }
        verify(exactly = 1) { databaseBuilder.build() }
        verify(exactly = 1) { context.getApplicationContext() }
    }

}