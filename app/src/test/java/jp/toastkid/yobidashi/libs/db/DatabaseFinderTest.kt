/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.db

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

class DatabaseFinderTest {

    private lateinit var databaseFinder: DatabaseFinder

    @MockK
    private lateinit var roomDatabaseBuilder: RoomDatabase.Builder<AppDatabase>

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { roomDatabaseBuilder.fallbackToDestructiveMigration() }.returns(roomDatabaseBuilder)
        every { roomDatabaseBuilder.build() }.returns(mockk())

        mockkStatic(Room::class)
        every { Room.databaseBuilder(any(), any<Class<AppDatabase>>(), any()) }.returns(roomDatabaseBuilder)

        every { context.getApplicationContext() }.returns(mockk())

        databaseFinder = DatabaseFinder()
    }

    @After
    fun tearDown() {
        unmockkAll()
        DatabaseFinder.clearInstance()
    }

    @Test
    fun test() {
        databaseFinder.invoke(context)

        verify(atLeast = 1) { roomDatabaseBuilder.fallbackToDestructiveMigration() }
        verify(atLeast = 1) { roomDatabaseBuilder.build() }
        verify(atLeast = 1) { Room.databaseBuilder(any(), any<Class<AppDatabase>>(), any()) }
        verify(atLeast = 1) { context.getApplicationContext() }
    }

}