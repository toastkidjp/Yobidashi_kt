/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.clip

import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClippingUrlOpenerTest {

    @MockK
    private lateinit var view: View

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { view.getContext() }.returns(mockk())

        mockkObject(NetworkChecker)
        every { NetworkChecker.isNotAvailable(any()) }.returns(false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
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
    }

}