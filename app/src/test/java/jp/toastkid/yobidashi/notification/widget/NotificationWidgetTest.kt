/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.widget

import androidx.core.app.NotificationManagerCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class NotificationWidgetTest {

    @MockK
    private lateinit var notificationManagerCompat: NotificationManagerCompat

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { notificationManagerCompat.cancel(any()) }.answers { Unit }

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) }.returns(notificationManagerCompat)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun show() {
    }

    @Test
    fun hide() {
        NotificationWidget.hide(mockk())

        verify(exactly = 1) { NotificationManagerCompat.from(any()) }
        verify(exactly = 1) { notificationManagerCompat.cancel(any()) }
    }

    @Test
    fun refresh() {
    }

}