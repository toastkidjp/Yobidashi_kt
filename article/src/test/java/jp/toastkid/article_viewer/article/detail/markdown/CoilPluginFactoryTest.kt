/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.markdown

import android.app.ActivityManager
import android.content.Context
import coil.ImageLoader
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import io.noties.markwon.image.coil.CoilImagesPlugin

class CoilPluginFactoryTest {

    @InjectMockKs
    private lateinit var coilPluginFactory: CoilPluginFactory

    @MockK
    private lateinit var imageLoader: ImageLoader

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var activityManager: ActivityManager

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext }.returns(context)
        every { context.getSystemService(any()) }.returns(activityManager)

        mockkConstructor(ImageLoader.Builder::class)
        every { anyConstructed<ImageLoader.Builder>().build() }.returns(imageLoader)

        mockkStatic(CoilImagesPlugin::class)
        every { CoilImagesPlugin.create(any<CoilImagesPlugin.CoilStore>(), any()) }.returns(mockk())
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun testInvoke() {
        coilPluginFactory.invoke(context)

        verify(exactly = 1) { context.applicationContext }
        verify(exactly = 1) { context.getSystemService(any()) }
        verify(exactly = 1) { anyConstructed<ImageLoader.Builder>().build() }
        verify(exactly = 1) { CoilImagesPlugin.create(any<CoilImagesPlugin.CoilStore>(), any()) }
    }
}