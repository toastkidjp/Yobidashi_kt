/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.appwidget.search

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
import jp.toastkid.yobidashi.libs.intent.PendingIntentFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class RemoteViewsFactoryTest {

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getPackageName() }.returns("test")
        every { context.getSharedPreferences(any(), any()) }.returns(mockk(relaxed = true))

        mockkConstructor(RemoteViews::class)
        every { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }.answers { Unit }
        every { anyConstructed<RemoteViews>().setTextColor(any(), any()) }.answers { Unit }
        every { anyConstructed<RemoteViews>().setOnClickPendingIntent(any(), any()) }.answers { Unit }

        mockkConstructor(PreferenceApplier::class)

        mockkConstructor(PendingIntentFactory::class)
        every { anyConstructed<PendingIntentFactory>().makeSearchLauncher(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().launcher(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().barcode(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun make() {
        RemoteViewsFactory.make(context)

        verify(exactly = 1) { context.getPackageName() }
        verify(exactly = 1) { context.getSharedPreferences(any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setTextColor(any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setOnClickPendingIntent(any(), any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().makeSearchLauncher(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().launcher(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().barcode(any()) }
    }
}