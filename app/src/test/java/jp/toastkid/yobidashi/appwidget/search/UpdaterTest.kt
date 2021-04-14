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
import android.content.ContextWrapper
import android.content.SharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
import org.junit.After
import org.junit.Before
import org.junit.Test

class UpdaterTest {

    private lateinit var updater: Updater

    @MockK
    private lateinit var contextWrapper: ContextWrapper

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        updater = Updater()

        MockKAnnotations.init(this)
        every { contextWrapper.applicationContext }.returns(context)
        every { contextWrapper.getSharedPreferences(any(), any()) }.returns(sharedPreferences)
        every { sharedPreferences.getBoolean(any(), any()) }.returns(true)

        mockkStatic(AppWidgetManager::class)
        every { AppWidgetManager.getInstance(any()) }.returns(mockk())

        mockkObject(RemoteViewsFactory)
        every { RemoteViewsFactory.make(any()) }.returns(mockk())

        mockkObject(Provider)
        every { Provider.updateWidget(any(), any(), any()) }.answers { Unit }

        mockkObject(NotificationWidget)
        every { NotificationWidget.refresh(any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testUpdate() {
        updater.update(contextWrapper)

        verify(exactly = 1) { contextWrapper.applicationContext }
        verify(exactly = 1) { contextWrapper.getSharedPreferences(any(), any()) }
        verify(exactly = 1) { AppWidgetManager.getInstance(any()) }
        verify(exactly = 1) { RemoteViewsFactory.make(any()) }
        verify(exactly = 1) { Provider.updateWidget(any(), any(), any()) }
        verify(exactly = 1) { NotificationWidget.refresh(any()) }
    }

    @Test
    fun falseCase() {
        every { sharedPreferences.getBoolean(any(), any()) }.returns(false)

        updater.update(contextWrapper)

        verify(exactly = 1) { contextWrapper.applicationContext }
        verify(exactly = 1) { contextWrapper.getSharedPreferences(any(), any()) }
        verify(exactly = 1) { AppWidgetManager.getInstance(any()) }
        verify(exactly = 1) { RemoteViewsFactory.make(any()) }
        verify(exactly = 1) { Provider.updateWidget(any(), any(), any()) }
        verify(exactly = 0) { NotificationWidget.refresh(any()) }
    }

}