/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class BookmarkInsertionTest {

    private lateinit var bookmarkInsertion: BookmarkInsertion

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(DatabaseFinder::class)
        coEvery { anyConstructed<DatabaseFinder>().invoke(any()) }.returns(appDatabase)
        coEvery { appDatabase.bookmarkRepository() }.returns(bookmarkRepository)
        coEvery { bookmarkRepository.add(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testEmptyTitleCase() {
        bookmarkInsertion = BookmarkInsertion(context, "", "https://www.yahoo.co.jp")

        bookmarkInsertion.insert()

        coVerify(exactly = 0) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 0) { appDatabase.bookmarkRepository() }
        coVerify(exactly = 0) { bookmarkRepository.add(any()) }
    }

    @Test
    fun test() {
        bookmarkInsertion = BookmarkInsertion(
            context,
            "Yahoo! JAPAN",
            "https://www.yahoo.co.jp",
            dispatcher = TestCoroutineDispatcher()
        )

        bookmarkInsertion.insert()

        coVerify(exactly = 1) { anyConstructed<DatabaseFinder>().invoke(any()) }
        coVerify(exactly = 1) { appDatabase.bookmarkRepository() }
        coVerify(exactly = 1) { bookmarkRepository.add(any()) }
    }

}