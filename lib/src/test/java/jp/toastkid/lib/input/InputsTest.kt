/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.input

import android.app.Activity
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class InputsTest {

    @MockK
    private lateinit var inputMethodManager: InputMethodManager

    @MockK
    private lateinit var view: EditText

    @MockK
    private lateinit var context: Activity

    @MockK
    private lateinit var window: Window

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { view.context }.returns(context)
        every { view.windowToken }.returns(mockk())
        every { context.getSystemService(any()) }.returns(inputMethodManager)
        every { inputMethodManager.isActive }.returns(true)
        every { inputMethodManager.showSoftInput(any(), any()) }.returns(true)
        every { inputMethodManager.hideSoftInputFromWindow(any(), any()) }.returns(true)
        every { window.setSoftInputMode(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testHideKeyboard() {
        Inputs().hideKeyboard(view)

        verify(exactly = 1) { context.getSystemService(any()) }
        verify(exactly = 1) { view.windowToken }
        verify(exactly = 1) { inputMethodManager.isActive }
        verify(exactly = 1) { inputMethodManager.hideSoftInputFromWindow(any(), any()) }
    }

}