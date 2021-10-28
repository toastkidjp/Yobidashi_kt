/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.appwidget.search

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
import jp.toastkid.yobidashi.libs.intent.PendingIntentFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class RemoteViewsFactoryTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getPackageName() }.returns("test")
        every { context.getResources() }.returns(resources)
        every { context.getSharedPreferences(any(), any()) }.returns(mockk(relaxed = true))
        every { resources.getColor(any()) }.returns(Color.BLACK)

        mockkConstructor(RemoteViews::class)
        every { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }.just(Runs)
        every { anyConstructed<RemoteViews>().setTextColor(any(), any()) }.just(Runs)
        every { anyConstructed<RemoteViews>().setOnClickPendingIntent(any(), any()) }.just(Runs)

        mockkConstructor(PreferenceApplier::class)

        mockkConstructor(PendingIntentFactory::class)
        every { anyConstructed<PendingIntentFactory>().makeSearchLauncher(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().barcode(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testMake() {
        RemoteViewsFactory.make(context)

        verify(exactly = 1) { context.packageName }
        verify(exactly = 1) { context.getSharedPreferences(any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setInt(any(), any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setTextColor(any(), any()) }
        verify(atLeast = 1) { anyConstructed<RemoteViews>().setOnClickPendingIntent(any(), any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().makeSearchLauncher(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().barcode(any()) }
    }

}