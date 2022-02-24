/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.preference

import android.graphics.Color
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ColorPairTest {

    private lateinit var colorPair: ColorPair

    @Before
    fun setUp() {
        colorPair = ColorPair(Color.BLACK, Color.WHITE)
    }

    @Test
    fun bgColor() {
        assertEquals(Color.BLACK, colorPair.bgColor())
    }

    @Test
    fun fontColor() {
        assertEquals(Color.WHITE, colorPair.fontColor())
    }

    @Test
    fun setTo() {
        val textView = mockk<TextView>()
        every { textView.setBackgroundColor(any()) }.answers { Unit }
        every { textView.setTextColor(any<Int>()) }.answers { Unit }
        every { textView.getCompoundDrawables() }.answers { arrayOf() }

        colorPair.setTo(textView)

        verify (exactly = 1) { textView.setBackgroundColor(any()) }
        verify (exactly = 1) { textView.setTextColor(any<Int>()) }
        verify (exactly = 1) { textView.getCompoundDrawables() }
    }

    @Test
    fun setToWithCompoundDrawable() {
        val textView = mockk<TextView>()
        every { textView.setBackgroundColor(any()) }.answers { Unit }
        every { textView.setTextColor(any<Int>()) }.answers { Unit }
        val compoundDrawable = mockk<Drawable>()
        every { compoundDrawable.colorFilter = any() }.just(Runs)
        every { textView.getCompoundDrawables() }.answers { arrayOf(compoundDrawable) }
        mockkConstructor(PorterDuffColorFilter::class)

        colorPair.setTo(textView)

        verify (exactly = 1) { textView.setBackgroundColor(any()) }
        verify (exactly = 1) { textView.setTextColor(any<Int>()) }
        verify (exactly = 1) { textView.getCompoundDrawables() }
    }

    @Test
    fun applyTo() {
        val fab = mockk<FloatingActionButton>()
        every { fab.setBackgroundTintList(any()) }.answers { Unit }
        every { fab.getDrawable() }.answers { mockk() }
        every { fab.setImageDrawable(any()) }.answers { Unit }

        colorPair.applyTo(fab)

        verify (exactly = 1) { fab.setBackgroundTintList(any()) }
        verify (exactly = 1) { fab.getDrawable() }
        verify (exactly = 1) { fab.setImageDrawable(any()) }
    }

    @Test
    fun applyReverseTo() {
        val fab = mockk<FloatingActionButton>()
        every { fab.setBackgroundTintList(any()) }.answers { Unit }
        every { fab.getDrawable() }.answers { mockk() }
        every { fab.setImageDrawable(any()) }.answers { Unit }

        colorPair.applyReverseTo(fab)

        verify (exactly = 1) { fab.setBackgroundTintList(any()) }
        verify (exactly = 1) { fab.getDrawable() }
        verify (exactly = 1) { fab.setImageDrawable(any()) }
    }


    @After
    fun tearDown() {
        unmockkAll()
    }

}