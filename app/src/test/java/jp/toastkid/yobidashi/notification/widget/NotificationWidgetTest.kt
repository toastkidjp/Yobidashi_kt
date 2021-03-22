/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.widget

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class NotificationWidgetTest {

    private lateinit var notificationWidget: NotificationWidget

    @MockK
    private lateinit var notificationManagerCompat: NotificationManagerCompat

    @MockK
    private lateinit var builder: NotificationCompat.Builder

    @MockK
    private lateinit var notification: Notification

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { notificationManagerCompat.cancel(any()) }.answers { Unit }
        every { notificationManagerCompat.notify(any(), any()) }.answers { Unit }
        every { context.getPackageName() }.returns("test")

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) }.returns(notificationManagerCompat)

        mockkConstructor(RemoteViewsFactory::class)
        every { anyConstructed<RemoteViewsFactory>().invoke(any()) }.returns(mockk())

        mockkConstructor(NotificationCompat.Builder::class)
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any()) }.returns(builder)
        every { builder.setCustomContentView(any()) }.returns(builder)
        every { builder.setOngoing(any()) }.returns(builder)
        every { builder.setAutoCancel(any()) }.returns(builder)
        every { builder.setVisibility(any()) }.returns(builder)
        every { builder.build() }.returns(notification)

        notificationWidget = spyk(NotificationWidget, recordPrivateCalls = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun refresh() {
        notificationWidget.refresh(context)

        verify(atLeast = 1) { NotificationManagerCompat.from(any()) }
        verify(exactly = 1) { notificationManagerCompat.cancel(any()) }
        verify(exactly = 1) { notificationManagerCompat.notify(any(), any()) }
        verify(exactly = 1) { anyConstructed<RemoteViewsFactory>().invoke(any()) }
        verify(exactly = 1) { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any()) }
        verify(exactly = 1) { builder.setCustomContentView(any()) }
        verify(exactly = 1) { builder.setOngoing(any()) }
        verify(exactly = 1) { builder.setAutoCancel(any()) }
        verify(exactly = 1) { builder.setVisibility(any()) }
        verify(exactly = 1) { builder.build() }
    }

}