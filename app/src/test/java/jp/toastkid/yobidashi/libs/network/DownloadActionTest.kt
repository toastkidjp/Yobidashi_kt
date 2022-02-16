/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.network

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.Toaster
import org.junit.After
import org.junit.Before
import org.junit.Test

class DownloadActionTest {

    @InjectMockKs
    private lateinit var downloadAction: DownloadAction

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getSharedPreferences(any(), any()) }.returns(mockk())

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().wifiOnly }.returns(true)

        mockkObject(NetworkChecker)
        every { NetworkChecker.isUnavailableWiFi(any()) }.returns(false)
        mockkObject(Toaster)
        every { Toaster.tShort(any(), any<Int>()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNoopOnUnavailableWiFiCase() {
        every { NetworkChecker.isUnavailableWiFi(any()) }.returns(true)

        downloadAction.invoke("https://www.search.yahoo.co.jp")

        verify { Toaster.tShort(any(), any<Int>()) }
    }
}