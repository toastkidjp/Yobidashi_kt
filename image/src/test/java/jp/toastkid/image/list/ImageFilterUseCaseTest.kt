/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.list

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageFilterUseCaseTest {

    @InjectMockKs
    private lateinit var imageFilterUseCase: ImageFilterUseCase

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var adapter: Adapter

    @MockK
    private lateinit var imageLoaderUseCase: ImageLoaderUseCase

    @MockK
    private lateinit var imageLoader: ImageLoader

    @MockK
    private lateinit var refreshContent: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { imageLoaderUseCase.invoke() }.just(Runs)
        every { imageLoaderUseCase.clearCurrentBucket() }.just(Runs)
        every { preferenceApplier.excludedItems() }.returns(emptySet())
        every { imageLoader.filterBy(any()) }.returns(emptyList())
        every { adapter.submitList(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        imageFilterUseCase.invoke(null)

        verify { imageLoaderUseCase.invoke() }
        verify(inverse = true) { imageLoaderUseCase.clearCurrentBucket() }
    }
}