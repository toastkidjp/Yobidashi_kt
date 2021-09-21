/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.list

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.image.Image
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageLoaderUseCaseTest {

    @InjectMockKs
    private lateinit var imageLoaderUseCase: ImageLoaderUseCase

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var adapter: Adapter

    @MockK
    private lateinit var bucketLoader: BucketLoader

    @MockK
    private lateinit var imageLoader: ImageLoader

    @MockK
    private lateinit var refreshContent: () -> Unit

    @MockK
    private lateinit var parentExtractor: ParentExtractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { adapter.clear() }.just(Runs)
        every { adapter.add(any()) }.just(Runs)
        every { refreshContent.invoke() }.just(Runs)
        every { preferenceApplier.excludedItems() }.returns(setOf("test"))
        every { preferenceApplier.imageViewerSort() }.returns("test")
        every { parentExtractor.invoke(any()) }.returns("to")

        val image = spyk(Image("/path/to/file", "name"))
        every { bucketLoader.invoke(any()) }.returns(listOf(image))
        every { imageLoader.invoke(any(), any()) }.returns(listOf(image))

        mockkConstructor(ExcludingItemFilter::class)
        every { anyConstructed<ExcludingItemFilter>().invoke(any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        imageLoaderUseCase.invoke()

        verify(exactly = 1) { adapter.clear() }
        verify(exactly = 1) { bucketLoader.invoke(any()) }
        verify(exactly = 0) { imageLoader.invoke(any(), any()) }
        verify(exactly = 1) { adapter.add(any()) }
    }

}