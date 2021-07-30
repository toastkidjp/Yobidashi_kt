/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.history

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class ViewHistoryInsertionTest {

    private lateinit var viewHistoryInsertion: ViewHistoryInsertion

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var repository: ViewHistoryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(DatabaseFinder::class)
        coEvery { anyConstructed<DatabaseFinder>().invoke(any()) }.returns(appDatabase)

        coEvery { appDatabase.viewHistoryRepository() }.returns(repository)
        coEvery { repository.add(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        viewHistoryInsertion = ViewHistoryInsertion.make(mockk(), "test", "test", "test", Dispatchers.Unconfined)

        viewHistoryInsertion.invoke()

        coVerify(exactly = 1) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 1) { appDatabase.viewHistoryRepository() }
        coVerify(exactly = 1) { repository.add(any()) }
    }

    @Test
    fun testTitleEmptyCase() {
        viewHistoryInsertion = ViewHistoryInsertion.make(mockk(), "", "test", "test", Dispatchers.Unconfined)

        viewHistoryInsertion.invoke()

        coVerify(exactly = 1) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 1) { appDatabase.viewHistoryRepository() }
        coVerify(exactly = 0) { repository.add(any()) }
    }

    @Test
    fun testUrlEmptyCase() {
        viewHistoryInsertion = ViewHistoryInsertion.make(mockk(), "test", "", "test", Dispatchers.Unconfined)

        viewHistoryInsertion.invoke()

        coVerify(exactly = 1) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 1) { appDatabase.viewHistoryRepository() }
        coVerify(exactly = 0) { repository.add(any()) }
    }

}