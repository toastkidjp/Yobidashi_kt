/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.intent

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.launch.MainActivityIntentFactory
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import org.junit.After
import org.junit.Before
import org.junit.Test

class PendingIntentFactoryTest {

    @InjectMockKs
    private lateinit var pendingIntentFactory: PendingIntentFactory

    @MockK
    private lateinit var mainActivityIntentFactory: MainActivityIntentFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { mainActivityIntentFactory.barcodeReader(any()) }.returns(mockk())
        every { mainActivityIntentFactory.bookmark(any()) }.returns(mockk())
        every { mainActivityIntentFactory.browser(any(), any()) }.returns(mockk())
        every { mainActivityIntentFactory.launcher(any()) }.returns(mockk())
        every { mainActivityIntentFactory.search(any()) }.returns(mockk())
        every { mainActivityIntentFactory.setting(any()) }.returns(mockk())

        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) }.returns(mockk())

        mockkObject(RandomWikipedia)
        every { RandomWikipedia.makeIntent(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun makeSearchLauncher() {
        pendingIntentFactory.makeSearchLauncher(mockk())

        verify(exactly = 1) { mainActivityIntentFactory.search(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }
    }

    @Test
    fun launcher() {
        pendingIntentFactory.launcher(mockk())

        verify(exactly = 1) { mainActivityIntentFactory.launcher(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }
    }

    @Test
    fun barcode() {
        pendingIntentFactory.barcode(mockk())

        verify(exactly = 1) { mainActivityIntentFactory.barcodeReader(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }
    }

    @Test
    fun browser() {
        val context = mockk<Context>()
        every { context.getSharedPreferences(any(), any()) }.returns(mockk())

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().homeUrl }.returns("https://www.yahoo.co.jp")

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        pendingIntentFactory.browser(context)

        verify(exactly = 1) { mainActivityIntentFactory.browser(any(), any()) }
        verify(exactly = 1) { context.getSharedPreferences(any(), any()) }
        verify(exactly = 1) { Uri.parse(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }

        unmockkStatic(Uri::class)
    }

    @Test
    fun bookmark() {
        pendingIntentFactory.bookmark(mockk())

        verify(exactly = 1) { mainActivityIntentFactory.bookmark(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }
    }

    @Test
    fun randomWikipedia() {
        pendingIntentFactory.randomWikipedia(mockk())

        verify(exactly = 1) { RandomWikipedia.makeIntent(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }
    }

    @Test
    fun setting() {
        pendingIntentFactory.setting(mockk())

        verify(exactly = 1) { mainActivityIntentFactory.setting(any()) }
        verify(exactly = 1) { PendingIntent.getActivity(any(), any(), any(), any()) }
    }

}