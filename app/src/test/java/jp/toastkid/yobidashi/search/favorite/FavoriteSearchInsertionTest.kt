/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class FavoriteSearchInsertionTest {

    private lateinit var favoriteSearchInsertion: FavoriteSearchInsertion

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var repository: FavoriteSearchRepository

    @MockK
    private lateinit var context: Context

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getString(any(), any()) }.returns("test message")

        mockkConstructor(DatabaseFinder::class)
        every { anyConstructed<DatabaseFinder>().invoke(any()) }.returns(appDatabase)
        every { appDatabase.favoriteSearchRepository() }.returns(repository)
        every { repository.insert(any()) }.answers { Unit }

        mockkObject(Toaster)
        every { Toaster.tShort(any(), any<String>()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        favoriteSearchInsertion = FavoriteSearchInsertion(
            context,
            "category",
            "query",
            Dispatchers.Unconfined,
            Dispatchers.Unconfined
        )

        favoriteSearchInsertion.invoke()

        verify(exactly = 1) { context.getString(any(), any()) }
        verify(exactly = 1) { anyConstructed<DatabaseFinder>().invoke(any()) }
        verify(exactly = 1) { appDatabase.favoriteSearchRepository() }
        verify(exactly = 1) { repository.insert(any()) }
        verify(exactly = 1) { Toaster.tShort(any(), any<String>()) }
    }

}