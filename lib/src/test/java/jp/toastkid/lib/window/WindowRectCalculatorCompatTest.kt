/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.window

import android.app.Activity
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class WindowRectCalculatorCompatTest {

    @InjectMockKs
    private lateinit var windowRectCalculatorCompat: WindowRectCalculatorCompat

    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var windowManager: WindowManager

    @MockK
    private lateinit var windowMetrics: WindowMetrics

    @MockK
    private lateinit var defaultDisplay: Display

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { activity.windowManager }.returns(windowManager)
        every { windowManager.currentWindowMetrics }.returns(windowMetrics)
        every { windowMetrics.bounds }.returns(mockk())
        every { windowManager.defaultDisplay }.returns(defaultDisplay)
        every { defaultDisplay.getRectSize(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testRAndOver() {
        windowRectCalculatorCompat.invoke(activity, 30)

        verify(exactly = 1) { activity.windowManager }
        verify(exactly = 1) { windowManager.currentWindowMetrics }
        verify(exactly = 1) { windowMetrics.bounds }
        verify(exactly = 0) { windowManager.defaultDisplay }
        verify(exactly = 0) { defaultDisplay.getRectSize(any()) }
    }

    @Test
    fun testUnderR() {
        windowRectCalculatorCompat.invoke(activity, 29)

        verify(exactly = 1) { activity.windowManager }
        verify(exactly = 0) { windowManager.currentWindowMetrics }
        verify(exactly = 0) { windowMetrics.bounds }
        verify(exactly = 1) { windowManager.defaultDisplay }
        verify(exactly = 1) { defaultDisplay.getRectSize(any()) }
    }

}