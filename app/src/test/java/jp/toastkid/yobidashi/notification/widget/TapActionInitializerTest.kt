/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.widget

import android.widget.RemoteViews
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.intent.PendingIntentFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class TapActionInitializerTest {

    private lateinit var tapActionInitializer: TapActionInitializer

    @MockK
    private lateinit var remoteViews: RemoteViews

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { remoteViews.setOnClickPendingIntent(any(), any()) }.answers { Unit }

        tapActionInitializer = TapActionInitializer()

        mockkConstructor(PendingIntentFactory::class)
        every { anyConstructed<PendingIntentFactory>().setting(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().makeSearchLauncher(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().randomWikipedia(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().barcode(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().browser(any()) }.returns(mockk())
        every { anyConstructed<PendingIntentFactory>().bookmark(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        tapActionInitializer.invoke(mockk(), remoteViews)

        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().setting(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().makeSearchLauncher(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().randomWikipedia(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().barcode(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().browser(any()) }
        verify(exactly = 1) { anyConstructed<PendingIntentFactory>().bookmark(any()) }
    }

}