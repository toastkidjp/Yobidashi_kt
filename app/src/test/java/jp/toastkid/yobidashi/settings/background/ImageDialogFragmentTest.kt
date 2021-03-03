/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.background

import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageDialogFragmentTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putCharSequence(any(), any()) }.returns(mockk())
        every { anyConstructed<Bundle>().putParcelable(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun withBitmap() {
        ImageDialogFragment.withBitmap(mockk())

        verify(exactly = 1) { anyConstructed<Bundle>().putParcelable(any(), any()) }
    }

    @Test
    fun withUrl() {
        ImageDialogFragment.withUrl("test")

        verify(exactly = 1) { anyConstructed<Bundle>().putCharSequence(any(), any()) }
    }

}