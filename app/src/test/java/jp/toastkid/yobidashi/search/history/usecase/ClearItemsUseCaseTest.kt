/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.history.usecase

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.history.SearchHistoryRepository
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClearItemsUseCaseTest {

    @InjectMockKs
    private lateinit var clearItemsUseCase: ClearItemsUseCase

    @MockK
    private lateinit var adapterClearAll: () -> Unit

    @Suppress("unused")
    private val mainDispatcher = Dispatchers.Unconfined

    private val ioDispatcher = Dispatchers.Unconfined

    @MockK
    private lateinit var activity: FragmentActivity

    @MockK
    private lateinit var fragmentManager: FragmentManager

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var searchHistoryRepository: SearchHistoryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { adapterClearAll.invoke() }.just(Runs)
        coEvery { activity.supportFragmentManager }.returns(fragmentManager)
        coEvery { fragmentManager.popBackStack() }.just(Runs)

        mockkConstructor(DatabaseFinder::class)
        coEvery { anyConstructed<DatabaseFinder>().invoke(any()) }.returns(appDatabase)

        coEvery { appDatabase.searchHistoryRepository() }.returns(searchHistoryRepository)
        coEvery { searchHistoryRepository.deleteAll() }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        clearItemsUseCase.invoke(activity)

        coVerify(exactly = 1) { adapterClearAll.invoke() }
        coVerify(exactly = 1) { activity.supportFragmentManager }
        coVerify(exactly = 1) { fragmentManager.popBackStack() }
        coVerify(exactly = 1) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 1) { appDatabase.searchHistoryRepository() }
        coVerify(exactly = 1) { searchHistoryRepository.deleteAll() }
    }

    @Test
    fun testPassedNull() {
        clearItemsUseCase.invoke(null)

        coVerify(exactly = 0) { adapterClearAll.invoke() }
        coVerify(exactly = 0) { activity.supportFragmentManager }
        coVerify(exactly = 0) { fragmentManager.popBackStack() }
        coVerify(exactly = 0) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 0) { appDatabase.searchHistoryRepository() }
        coVerify(exactly = 0) { searchHistoryRepository.deleteAll() }
    }

}