/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.widget.RemoteViews
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class RemoteViewsFactoryTest {

    private lateinit var remoteViewsFactory: RemoteViewsFactory

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        remoteViewsFactory = RemoteViewsFactory()

        every { context.packageName }.returns("test")
        every { context.resources }.returns(resources)
        every { context.getSharedPreferences(any(), any()) }.returns(mockk(relaxed = true))
        every { @Suppress("DEPRECATION") resources.getColor(any()) }.returns(Color.BLACK)

        mockkConstructor(RemoteViews::class)
        every { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }.just(Runs)
        every { anyConstructed<RemoteViews>().setTextColor(any(), any()) }.just(Runs)

        mockkConstructor(PreferenceApplier::class)

        mockkConstructor(TapActionInitializer::class)
        every { anyConstructed<TapActionInitializer>().invoke(any(), any()) }.just(Runs)

        mockkConstructor(IconInitializer::class)
        every { anyConstructed<IconInitializer>().invoke(any(), any(), any(), any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        remoteViewsFactory.invoke(context)

        verify(exactly = 1) { context.getPackageName() }
        verify(exactly = 1) { context.getSharedPreferences(any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setTextColor(any(), any()) }
        verify(atLeast = 1) { anyConstructed<TapActionInitializer>().invoke(any(), any()) }
        verify(atLeast = 1) { anyConstructed<IconInitializer>().invoke(any(), any(), any(), any()) }
    }

}