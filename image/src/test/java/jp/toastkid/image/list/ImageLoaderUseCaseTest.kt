/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.list

import androidx.compose.runtime.mutableStateOf
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
import jp.toastkid.image.Image
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageLoaderUseCaseTest {

    @InjectMockKs
    private lateinit var imageLoaderUseCase: ImageLoaderUseCase

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var bucketLoader: BucketLoader

    @MockK
    private lateinit var imageLoader: ImageLoader

    private val backHandlerState = mutableStateOf(true)

    @MockK
    private lateinit var refreshContent: () -> Unit

    @MockK
    private lateinit var parentExtractor: ParentExtractor

    @MockK
    private lateinit var submitImages: (List<Image>) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { refreshContent.invoke() }.just(Runs)
        every { submitImages.invoke(any()) }.just(Runs)
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
    fun testInvoke() {
        imageLoaderUseCase.invoke()

        verify(exactly = 1) { bucketLoader.invoke(any()) }
        verify(exactly = 0) { imageLoader.invoke(any(), any()) }
        verify(exactly = 1) { submitImages(any()) }
    }

    @Test
    fun testInvokeWithBucket() {
        imageLoaderUseCase.invoke("test-bucket")

        verify(exactly = 0) { bucketLoader.invoke(any()) }
        verify(exactly = 1) { imageLoader.invoke(any(), any()) }
        verify(exactly = 1) { submitImages(any()) }
    }

    @Test
    fun testClearCurrentBucket() {
        imageLoaderUseCase.clearCurrentBucket()
    }

}