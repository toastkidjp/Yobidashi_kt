/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.list

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class BucketLoaderTest {

    @InjectMockKs
    private lateinit var bucketLoader: BucketLoader

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var externalContentUri: Uri

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { contentResolver.query(any(), any(), any(), any(), any()) }.returns(cursor)
        every { cursor.getColumnIndex(any()) }.returns(0)
        every { cursor.getString(any()) }.returns("test")
        every { cursor.moveToNext() }.returns(false)
        every { cursor.close() }.just(Runs)

        mockkConstructor(ParentExtractor::class)
        every { anyConstructed<ParentExtractor>().invoke(any()) }.returns("path")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testQueryReturnsNullCase() {
        every { contentResolver.query(any(), any(), any(), any(), any()) }.returns(null)

        bucketLoader.invoke(Sort.default())

        verify(inverse = true) { cursor.getColumnIndex(any()) }
        verify(inverse = true) { cursor.close() }
    }

    @Test
    fun test() {
        bucketLoader.invoke(Sort.default())

        verify { cursor.getColumnIndex(any()) }
        verify { cursor.close() }
    }
}