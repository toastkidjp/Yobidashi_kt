/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.Toaster
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class DarkModeApplierTest {

    private lateinit var darkModeApplier: DarkModeApplier

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var parent: View

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { parent.getContext() }.returns(mockk())
        every { preferenceApplier.colorPair() }.returns(mockk())

        mockkStatic(ContextCompat::class)
        every { ContextCompat.getColor(any(), any()) }.returns(Color.TRANSPARENT)

        mockkObject(Theme)
        every { Theme.extract(any(), any(), any()) }.returns(mockk())

        mockkStatic(Color::class)
        every { Color.parseColor(any()) }.returns(Color.TRANSPARENT)

        mockkConstructor(Theme::class)
        every { anyConstructed<Theme>().apply(any()) }.returns(Unit)

        mockkObject(Toaster)
        every { Toaster.snackLong(any(), any<String>(), any(), any(), any()) }.returns(mockk())

        darkModeApplier = DarkModeApplier()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        darkModeApplier.invoke(preferenceApplier, parent)

        verify(exactly = 1) { parent.getContext() }
        verify(exactly = 1) { preferenceApplier.colorPair() }
        verify(atLeast = 1) { ContextCompat.getColor(any(), any()) }
        verify(exactly = 1) { Theme.extract(any(), any(), any()) }
        verify(atLeast = 1) { Color.parseColor(any()) }
        verify(exactly = 1) { anyConstructed<Theme>().apply(any()) }
        verify(exactly = 1) { Toaster.snackLong(any(), any<String>(), any(), any(), any()) }
    }

}