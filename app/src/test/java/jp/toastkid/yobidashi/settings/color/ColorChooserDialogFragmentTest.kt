/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.color

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ColorChooserDialogFragmentTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putInt(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        val fragment = ColorChooserDialogFragment.withCurrentColor(Color.TRANSPARENT)

        assertTrue(fragment is Fragment)
        verify(exactly = 1) { anyConstructed<Bundle>().putInt(any(), any()) }
    }

}