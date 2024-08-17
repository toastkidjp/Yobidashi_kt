/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.music

import android.content.ContentResolver
import android.database.Cursor
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify

class MusicFileFinderTest {

    @InjectMockKs
    private lateinit var musicFileFinder: MusicFileFinder

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var cursor: Cursor

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { contentResolver.query(any(), any(), any(), any(), any()) }.returns(cursor)
        every { cursor.moveToNext() }.returns(false)
        every { cursor.close() }.just(Runs)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        musicFileFinder.invoke()

        verify { contentResolver.query(any(), any(), any(), any(), any()) }
        verify { cursor.close() }
    }
}