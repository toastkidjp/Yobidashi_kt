/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.music

import android.content.ContentResolver
import android.graphics.BitmapFactory
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.InputStream

/**
 * @author toastkidjp
 */
class AlbumArtFinderTest {

    @InjectMockKs
    private lateinit var albumArtFinder: AlbumArtFinder

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var inputStream: InputStream

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { contentResolver.openInputStream(any()) }.returns(inputStream)
        every { inputStream.close() }.returns(Unit)

        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeStream(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        albumArtFinder.invoke(mockk())

        verify(exactly = 1) { contentResolver.openInputStream(any()) }
        verify(exactly = 1) { inputStream.close() }
        verify(exactly = 1) { BitmapFactory.decodeStream(any()) }
    }

}