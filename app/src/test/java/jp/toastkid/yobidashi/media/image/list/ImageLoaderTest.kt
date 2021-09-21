/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.list

import android.content.ContentResolver
import android.database.Cursor
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageLoaderTest {

    @InjectMockKs
    private lateinit var imageLoader: ImageLoader

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { contentResolver.query(any(), any(), any(), any(), any()) }.returns(cursor)
        every { cursor.getColumnIndex(any()) }.returns(0)
        every { cursor.moveToNext() }.returns(false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        imageLoader.invoke(Sort.DATE, "test-bucket")

        verify(exactly = 1) { contentResolver.query(any(), any(), any(), any(), any()) }
        verify(atLeast = 1) { cursor.getColumnIndex(any()) }
        verify(exactly = 1) { cursor.moveToNext() }
    }

}