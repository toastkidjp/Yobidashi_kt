/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
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

class ReceiverTest {

    private lateinit var receiver: Receiver

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var intent: Intent

    @MockK
    private lateinit var appWidgetManager: AppWidgetManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { intent.getAction() }.returns(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        every { context.getApplicationContext() }.returns(mockk())

        mockkObject(Provider.Companion)
        every { Provider.updateWidget(any(), any(), any()) }.answers { Unit }

        mockkObject(RemoteViewsFactory)
        every { RemoteViewsFactory.make(any()) }.returns(mockk())

        mockkStatic(AppWidgetManager::class)
        every { AppWidgetManager.getInstance(any()) }.returns(appWidgetManager)

        receiver = Receiver()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOnReceive() {
        receiver.onReceive(context, intent)

        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { context.getApplicationContext() }
        verify(exactly = 1) { Provider.updateWidget(any(), any(), any()) }
        verify(exactly = 1) { RemoteViewsFactory.make(any()) }
    }

}