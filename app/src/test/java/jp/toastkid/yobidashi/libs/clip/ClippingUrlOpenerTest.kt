/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.clip

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.main.usecase.ClippingUrlOpener
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClippingUrlOpenerTest {

    private lateinit var clippingUrlOpener: ClippingUrlOpener

    @MockK
    private lateinit var context: ComponentActivity

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @Before
    fun setUp() {
        clippingUrlOpener = ClippingUrlOpener()
        
        MockKAnnotations.init(this)
        every { context.getString(any(), any()) }.returns("Test message")
        every { context.getSharedPreferences(any(), any()) }.returns(mockk())

        mockkConstructor(NetworkChecker::class)
        every { anyConstructed<NetworkChecker>().isNotAvailable(any()) }.returns(false)

        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("clipped")

        mockkObject(Urls)
        every { Urls.isInvalidUrl(any()) }.returns(false)

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().lastClippedWord() }.returns("last-clipped")
        every { anyConstructed<PreferenceApplier>().setLastClippedWord(any()) }.returns(Unit)

        mockkConstructor(ViewModelProvider::class)
        every { anyConstructed<ViewModelProvider>().get(ContentViewModel::class.java) }.returns(contentViewModel)
        every { contentViewModel.snackWithAction(any(), any(), any()) }.just(Runs)

        every { context.viewModelStore }.returns(mockk())
        every { context.defaultViewModelProviderFactory }.returns(mockk())
        every { context.defaultViewModelCreationExtras }.returns(mockk())
        every { context.getString(any()) }.returns("jp.toastkid.yobidashi")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testParentViewIsNull() {
        clippingUrlOpener.invoke(null) { }

        verify(exactly = 0) { anyConstructed<NetworkChecker>().isNotAvailable(any()) }
    }

    @Test
    fun testNetworkIsNotAvailable() {
        every { anyConstructed<NetworkChecker>().isNotAvailable(any()) }.returns(true)

        clippingUrlOpener.invoke(context) { }

        verify(exactly = 1) { anyConstructed<NetworkChecker>().isNotAvailable(any()) }
        verify(exactly = 0) { Clipboard.getPrimary(any()) }
    }

    @Test
    fun testClipboardIsNull() {
        every { Clipboard.getPrimary(any()) }.returns(null)

        clippingUrlOpener.invoke(context) { }

        verify(exactly = 1) { anyConstructed<NetworkChecker>().isNotAvailable(any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 0) { Urls.isInvalidUrl(any()) }
    }

    @Test
    fun testClipboardIsInvalidUrl() {
        every { Clipboard.getPrimary(any()) }.returns("test")
        every { Urls.isInvalidUrl(any()) }.returns(true)

        clippingUrlOpener.invoke(context) { }

        verify(exactly = 1) { anyConstructed<NetworkChecker>().isNotAvailable(any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 1) { Urls.isInvalidUrl(any()) }
    }

    @Test
    fun testClipboardCorrectCase() {
        every { Clipboard.getPrimary(any()) }.returns("https://www.yahoo.co.jp")
        every { Urls.isInvalidUrl(any()) }.returns(false)

        clippingUrlOpener.invoke(context) { }

        verify(exactly = 1) { anyConstructed<NetworkChecker>().isNotAvailable(any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 1) { Urls.isInvalidUrl(any()) }
        verify(exactly = 1) { context.getString(any(), any()) }
        verify(exactly = 1) { anyConstructed<PreferenceApplier>().lastClippedWord() }
        verify(exactly = 1) { anyConstructed<PreferenceApplier>().setLastClippedWord(any()) }
    }

}