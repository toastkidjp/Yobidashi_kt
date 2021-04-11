/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.ColorPair
import org.junit.After
import org.junit.Before
import org.junit.Test

class ToasterTest {

    @MockK
    private lateinit var toast: Toast

    @MockK
    private lateinit var snackbar: Snackbar

    @MockK
    private lateinit var snackbarView: View

    @MockK
    private lateinit var view: View

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var colorPair: ColorPair

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { toast.show() }.answers { Unit }
        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<Int>(), any()) }.returns(toast)
        every { Toast.makeText(any(), any<String>(), any()) }.returns(toast)

        every { view.getContext() }.returns(context)
        every { context.getString(any()) }.returns("test")
        mockkStatic(Snackbar::class)
        every { Snackbar.make(any(), any<String>(), any()) }.returns(snackbar)
        val snackbarContentView = mockk<TextView>()
        every { snackbarView.setBackgroundColor(any()) }.answers { Unit }
        every { snackbarView.findViewById<View>(any()) }.returns(snackbarContentView)
        every { snackbarContentView.setTextColor(any<Int>()) }.answers { Unit }
        every { colorPair.fontColor() }.returns(Color.WHITE)
        every { colorPair.bgColor() }.returns(Color.BLACK)
        every { snackbar.getView() }.returns(snackbarView)
        every { snackbar.show() }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testToast() {
        Toaster.tShort(mockk(), -1)

        verify(exactly = 1) { Toast.makeText(any(), any<Int>(), any()) }
        verify(exactly = 0) { Toast.makeText(any(), any<String>(), any()) }
        verify(exactly = 1)  { toast.show() }
    }

    @Test
    fun toastShortWithString() {
        Toaster.tShort(mockk(), "test")

        verify(exactly = 0) { Toast.makeText(any(), any<Int>(), any()) }
        verify(exactly = 1) { Toast.makeText(any(), any<String>(), any()) }
        verify(exactly = 1)  { toast.show() }
    }

    @Test
    fun snackShort() {
        Toaster.snackShort(view, 1, colorPair)

        verify(exactly = 1) { view.getContext() }
        verify(exactly = 1) { context.getString(any()) }
        verify(exactly = 1) { snackbar.getView() }
        verify(exactly = 1) { snackbar.show() }
        verify(exactly = 1) { Snackbar.make(any(), any<String>(), any()) }
        verify(exactly = 1) { snackbarView.setBackgroundColor(any()) }
        verify(exactly = 1) { snackbarView.findViewById<View>(any()) }
        verify(exactly = 1) { colorPair.fontColor() }
        verify(exactly = 1) { colorPair.bgColor() }
    }

    @Test
    fun snackShortWithText() {
        Toaster.snackShort(view, "test", colorPair)

        verify(exactly = 0) { view.getContext() }
        verify(exactly = 0) { context.getString(any()) }
        verify(exactly = 1) { snackbar.getView() }
        verify(exactly = 1) { snackbar.show() }
        verify(exactly = 1) { Snackbar.make(any(), any<String>(), any()) }
        verify(exactly = 1) { snackbarView.setBackgroundColor(any()) }
        verify(exactly = 1) { snackbarView.findViewById<View>(any()) }
        verify(exactly = 1) { colorPair.fontColor() }
        verify(exactly = 1) { colorPair.bgColor() }
    }

    @Test
    fun snackIndefinite() {
        Toaster.snackIndefinite(view, "test", colorPair)

        verify(exactly = 0) { view.getContext() }
        verify(exactly = 0) { context.getString(any()) }
        verify(exactly = 1) { snackbar.getView() }
        verify(exactly = 1) { snackbar.show() }
        verify(exactly = 1) { Snackbar.make(any(), any<String>(), any()) }
        verify(exactly = 1) { snackbarView.setBackgroundColor(any()) }
        verify(exactly = 1) { snackbarView.findViewById<View>(any()) }
        verify(exactly = 1) { colorPair.fontColor() }
        verify(exactly = 1) { colorPair.bgColor() }
    }

}