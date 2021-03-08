/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.color

import android.graphics.Color
import androidx.core.content.ContextCompat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultColorInsertionTest {

    @InjectMockKs
    private lateinit var defaultColorInsertion: DefaultColorInsertion

    @MockK
    private lateinit var databaseFinder: DatabaseFinder

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var savedColorRepository: SavedColorRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { databaseFinder.invoke(any()) }.returns(appDatabase)
        coEvery { appDatabase.savedColorRepository() }.returns(savedColorRepository)
        coEvery { savedColorRepository.add(any()) }.answers { Unit }

        mockkStatic(ContextCompat::class)
        coEvery { ContextCompat.getColor(any(), any()) }.returns(Color.TRANSPARENT)

        mockkObject(SavedColor)
        coEvery { SavedColor.make(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInsert() = runBlockingTest {
        defaultColorInsertion.insert(mockk())

        delay(1000L)

        coVerify (exactly = 1) { databaseFinder.invoke(any()) }
        coVerify (exactly = 1) { appDatabase.savedColorRepository() }
        coVerify (atLeast = 1) { savedColorRepository.add(any()) }
        coVerify (atLeast = 1) { ContextCompat.getColor(any(), any()) }
        coVerify (atLeast = 1) { SavedColor.make(any(), any()) }
    }

}