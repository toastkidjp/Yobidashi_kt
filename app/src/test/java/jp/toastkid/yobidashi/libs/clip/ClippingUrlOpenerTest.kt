/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.clip

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClippingUrlOpenerTest {

    @Before
    fun setUp() {
        mockkObject(NetworkChecker)
        every { NetworkChecker.isNotAvailable(any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        ClippingUrlOpener.invoke(null, { })

        verify(exactly = 0) { NetworkChecker.isNotAvailable(any()) }
    }

}