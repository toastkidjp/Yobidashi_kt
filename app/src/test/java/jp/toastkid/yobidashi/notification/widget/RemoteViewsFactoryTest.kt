/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.widget

import android.content.Context
import android.widget.RemoteViews
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        remoteViewsFactory = RemoteViewsFactory()

        every { context.getPackageName() }.returns("test")
        every { context.getSharedPreferences(any(), any()) }.returns(mockk(relaxed = true))

        mockkConstructor(RemoteViews::class)
        every { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }.answers { Unit }
        every { anyConstructed<RemoteViews>().setTextColor(any(), any()) }.answers { Unit }

        mockkConstructor(PreferenceApplier::class)

        mockkConstructor(TapActionInitializer::class)
        every { anyConstructed<TapActionInitializer>().invoke(any(), any()) }.answers { Unit }

        mockkConstructor(IconInitializer::class)
        every { anyConstructed<IconInitializer>().invoke(any(), any(), any(), any()) }.answers { Unit }
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