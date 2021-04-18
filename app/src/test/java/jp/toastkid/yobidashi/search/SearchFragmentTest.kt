/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search

import android.os.Bundle
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchFragmentTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        SearchFragment.makeWith("title", "https://www.yahoo.co.jp")

        verify(atLeast = 1) { anyConstructed<Bundle>().putString(any(), any()) }
    }

}