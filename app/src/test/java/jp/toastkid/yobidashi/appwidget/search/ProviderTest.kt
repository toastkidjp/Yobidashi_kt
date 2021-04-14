/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ProviderTest {

    private lateinit var provider: Provider

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var appWidgetManager: AppWidgetManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(RemoteViewsFactory)
        every { RemoteViewsFactory.make(any()) }.returns(mockk())

        mockkStatic(AppWidgetManager::class)
        every { AppWidgetManager.getInstance(any()) }.returns(appWidgetManager)
        every { appWidgetManager.updateAppWidget(any<ComponentName>(), any()) }.answers { Unit }

        provider = Provider()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        provider.onUpdate(context, appWidgetManager, intArrayOf())

        verify(exactly = 1) { RemoteViewsFactory.make(any()) }
        verify(exactly = 0) { AppWidgetManager.getInstance(any()) }
        verify(exactly = 1) { appWidgetManager.updateAppWidget(any<ComponentName>(), any()) }
    }

}