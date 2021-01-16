/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CompoundDrawableColorApplierTest {

    private lateinit var compoundDrawableColorApplier: CompoundDrawableColorApplier

    @Before
    fun setUp() {
        compoundDrawableColorApplier = CompoundDrawableColorApplier()
    }

    @Test
    fun testInvoke() {
        val drawable = mockk<Drawable>()
        every { drawable.setColorFilter(any()) }.answers { Unit }
        val textView = mockk<TextView>()
        every { textView.getCompoundDrawables() }.returns(arrayOf(drawable))

        compoundDrawableColorApplier.invoke(Color.BLUE, textView)

        verify (atLeast = 1) { textView.getCompoundDrawables() }
        verify (atLeast = 1) { drawable.setColorFilter(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}