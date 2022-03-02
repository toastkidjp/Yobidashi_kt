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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class PageSearcherModuleTest {

    @InjectMockKs
    private lateinit var pageSearcherModule: PageSearcherModule

    @MockK
    private lateinit var viewStubProxy: ViewStubProxy

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewStubProxy.isInflated }.returns(true)
        every { viewStubProxy.root?.visibility }.returns(View.VISIBLE)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun isVisible() {
        assertTrue(pageSearcherModule.isVisible())
    }

}