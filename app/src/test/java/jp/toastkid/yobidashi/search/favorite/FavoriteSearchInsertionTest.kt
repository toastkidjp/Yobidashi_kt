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
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.data.repository.factory.RepositoryFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class FavoriteSearchInsertionTest {

    private lateinit var favoriteSearchInsertion: FavoriteSearchInsertion

    @MockK
    private lateinit var repository: FavoriteSearchRepository

    @MockK
    private lateinit var context: Context

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(RepositoryFactory::class)
        every { anyConstructed<RepositoryFactory>().favoriteSearchRepository(any()) }.returns(repository)
        every { repository.insert(any()) }.just(Runs)
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

        verify(exactly = 1) { anyConstructed<RepositoryFactory>().favoriteSearchRepository(any()) }
        verify(exactly = 1) { repository.insert(any()) }
    }

}