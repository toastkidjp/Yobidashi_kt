/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.page_search

import android.view.View
import androidx.databinding.ViewStubProxy
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.channels.Channel
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PageSearcherModuleTest {

    @InjectMockKs
    private lateinit var pageSearcherModule: PageSearcherModule

    @MockK
    private lateinit var viewStubProxy: ViewStubProxy

    @MockK
    private lateinit var pageSearcherModuleAnimator: PageSearcherModuleAnimator

    @MockK
    private lateinit var channel: Channel<String>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewStubProxy.isInflated }.returns(true)
        every { viewStubProxy.root?.visibility }.returns(View.VISIBLE)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIsVisible() {
        assertTrue(pageSearcherModule.isVisible())
    }

    @Test
    fun testNotYetInflationCase() {
        every { viewStubProxy.isInflated }.returns(false)

        assertFalse(pageSearcherModule.isVisible())
    }
}