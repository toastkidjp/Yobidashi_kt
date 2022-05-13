/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.factory

import android.content.Context
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.Decoder
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class GifImageLoaderFactoryTest {

    @InjectMockKs
    private lateinit var gifImageLoaderFactory: GifImageLoaderFactory

    @MockK
    private lateinit var builder: ImageLoader.Builder

    @MockK
    private lateinit var componentRegistryBuilder: ComponentRegistry.Builder

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(ImageLoader.Builder::class)
        every { anyConstructed<ImageLoader.Builder>().components(any<ComponentRegistry>()) }.returns(builder)
        every { builder.build() }.returns(mockk())
        mockkConstructor(ComponentRegistry.Builder::class)
        every { anyConstructed<ComponentRegistry.Builder>().add(any<Decoder.Factory>()) }.returns(componentRegistryBuilder)
        every { componentRegistryBuilder.build() }.returns(mockk())
        every { context.applicationContext }.returns(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        gifImageLoaderFactory.invoke(context)

        verify { builder.build() }
        verify { componentRegistryBuilder.build() }
    }

}