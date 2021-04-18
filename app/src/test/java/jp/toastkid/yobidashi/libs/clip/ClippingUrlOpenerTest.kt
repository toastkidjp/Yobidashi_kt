/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.clip

import android.content.Context
import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClippingUrlOpenerTest {

    @MockK
    private lateinit var view: View

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { view.getContext() }.returns(context)
        every { context.getSharedPreferences(any(), any()) }.returns(mockk())

        mockkObject(NetworkChecker)
        every { NetworkChecker.isNotAvailable(any()) }.returns(false)

        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("clipped")

        mockkObject(Urls)
        every { Urls.isInvalidUrl(any()) }.returns(false)

        mockkObject(Toaster)
        every { Toaster.withAction(any(), any<String>(), any<Int>(), any(), any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testParentViewIsNull() {
        ClippingUrlOpener.invoke(null, { })

        verify(exactly = 0) { view.getContext() }
        verify(exactly = 0) { NetworkChecker.isNotAvailable(any()) }
    }

    @Test
    fun testNetworkIsNotAvailable() {
        every { NetworkChecker.isNotAvailable(any()) }.returns(true)

        ClippingUrlOpener.invoke(view, { })

        verify(exactly = 1) { view.getContext() }
        verify(exactly = 1) { NetworkChecker.isNotAvailable(any()) }
        verify(exactly = 0) { Clipboard.getPrimary(any()) }
    }

    @Test
    fun testClipboardIsNull() {
        every { Clipboard.getPrimary(any()) }.returns(null)

        ClippingUrlOpener.invoke(view, { })

        verify(atLeast = 1) { view.getContext() }
        verify(exactly = 1) { NetworkChecker.isNotAvailable(any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 0) { Urls.isInvalidUrl(any()) }
    }

    @Test
    fun testClipboardIsInvalidUrl() {
        every { Clipboard.getPrimary(any()) }.returns("test")
        every { Urls.isInvalidUrl(any()) }.returns(true)

        ClippingUrlOpener.invoke(view) { }

        verify(atLeast = 1) { view.getContext() }
        verify(exactly = 1) { NetworkChecker.isNotAvailable(any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 1) { Urls.isInvalidUrl(any()) }
        verify(exactly = 0) { Toaster.withAction(any(), any<String>(), any<Int>(), any(), any(), any()) }
    }

    @Test
    fun testClipboardCorrectCase() {
        every { Clipboard.getPrimary(any()) }.returns("https://www.yahoo.co.jp")
        every { Urls.isInvalidUrl(any()) }.returns(false)

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().colorPair() }.returns(mockk())

        ClippingUrlOpener.invoke(view) { }

        verify(atLeast = 1) { view.getContext() }
        verify(exactly = 1) { NetworkChecker.isNotAvailable(any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 1) { Urls.isInvalidUrl(any()) }
        verify(exactly = 1) { Toaster.withAction(any(), any<String>(), any<Int>(), any(), any(), any()) }
        verify(exactly = 1) { anyConstructed<PreferenceApplier>().colorPair() }
    }

}