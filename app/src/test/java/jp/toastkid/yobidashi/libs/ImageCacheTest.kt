/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ImageCacheTest {

    @InjectMockKs
    private lateinit var imageCache: ImageCache

    @MockK
    private lateinit var fileProvider: (File?, String) -> File

    @MockK
    private lateinit var bitmapCompressor: BitmapCompressor

    @MockK
    private lateinit var parent: File

    @MockK
    private lateinit var cache: File

    @MockK
    private lateinit var result: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { fileProvider.invoke(parent, any()) }.returns(cache)
        every { fileProvider.invoke(cache, any()) }.returns(result)
        every { bitmapCompressor.invoke(any(), any()) }.answers { Unit }
        every { cache.exists() }.returns(true)
        every { cache.mkdirs() }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun saveBitmap() {
        imageCache.saveBitmap(parent, mockk())

        verify(exactly = 1) { fileProvider.invoke(parent, any()) }
        verify(exactly = 1) { fileProvider.invoke(cache, any()) }
        verify(exactly = 1) { bitmapCompressor.invoke(any(), any()) }
        verify(exactly = 1) { cache.exists() }
        verify(exactly = 0) { cache.mkdirs() }
    }

}