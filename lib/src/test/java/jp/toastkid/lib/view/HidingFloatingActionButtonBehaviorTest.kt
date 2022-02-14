/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HidingFloatingActionButtonBehaviorTest {

    private lateinit var hidingFloatingActionButtonBehavior: HidingFloatingActionButtonBehavior

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var attrs: AttributeSet

    @MockK
    private lateinit var typedArray: TypedArray

    @MockK
    private lateinit var child: FloatingActionButton

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.obtainStyledAttributes(any<AttributeSet>(), any()) }.returns(typedArray)
        every { typedArray.getBoolean(any(), any()) }.returns(true)
        every { typedArray.recycle() }.just(Runs)

        hidingFloatingActionButtonBehavior = HidingFloatingActionButtonBehavior(context, attrs)

        every { child.show() }.just(Runs)
        every { child.hide() }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOnInterceptTouchEventPassedCancelNoopCase() {
        val motionEvent = mockk<MotionEvent>()
        every { motionEvent.action }.returns(MotionEvent.ACTION_CANCEL)
        every { child.visibility }.returns(View.VISIBLE)
        mockkConstructor(FloatingActionButton.Behavior::class)
        every { anyConstructed<FloatingActionButton.Behavior>().onInterceptTouchEvent(any(), any(), any()) }.returns(true)

        hidingFloatingActionButtonBehavior.onInterceptTouchEvent(mockk(), child, motionEvent)

        verify(inverse = true) { child.show() }
    }

    @Test
    fun testOnInterceptTouchEventUnderValueCase() {
        val motionEvent = mockk<MotionEvent>()
        every { motionEvent.action }.returns(MotionEvent.ACTION_UP)
        every { motionEvent.rawY }.returns(-11f)
        every { child.visibility }.returns(View.GONE)
        mockkConstructor(FloatingActionButton.Behavior::class)
        every { anyConstructed<FloatingActionButton.Behavior>().onInterceptTouchEvent(any(), any(), any()) }.returns(true)

        hidingFloatingActionButtonBehavior.onInterceptTouchEvent(mockk(), child, motionEvent)

        verify(inverse = true) { child.show() }
    }

    @Test
    fun onInterceptTouchEvent() {
        val motionEvent = mockk<MotionEvent>()
        every { motionEvent.action }.returns(MotionEvent.ACTION_UP)
        every { motionEvent.rawY }.returns(0f)
        every { child.visibility }.returns(View.GONE)
        mockkConstructor(FloatingActionButton.Behavior::class)
        every { anyConstructed<FloatingActionButton.Behavior>().onInterceptTouchEvent(any(), any(), any()) }.returns(true)

        hidingFloatingActionButtonBehavior.onInterceptTouchEvent(mockk(), child, motionEvent)

        verify { child.show() }
    }

    @Test
    fun testOnStartNestedScroll() {
        assertTrue(
            hidingFloatingActionButtonBehavior.onStartNestedScroll(
                mockk(),
                child,
                mockk(),
                mockk(),
                ViewCompat.SCROLL_AXIS_VERTICAL,
                0
            )
        )
    }

    @Test
    fun testOnStartNestedScrollReturnsFalseCase() {
        assertFalse(
            hidingFloatingActionButtonBehavior.onStartNestedScroll(
                mockk(),
                child,
                mockk(),
                mockk(),
                ViewCompat.SCROLL_AXIS_HORIZONTAL,
                0
            )
        )
    }

    @Test
    fun testOnNestedPreScrollToHideVisibleButtonCase() {
        every { child.visibility }.returns(View.VISIBLE)

        hidingFloatingActionButtonBehavior.onNestedPreScroll(mockk(), child, mockk(), 0, 1, intArrayOf(), 0)

        verify(inverse = true) { child.show() }
        verify { child.hide() }
    }

    @Test
    fun testOnNestedPreScrollNoopCase() {
        every { child.visibility }.returns(View.GONE)

        hidingFloatingActionButtonBehavior.onNestedPreScroll(mockk(), child, mockk(), 0, 1, intArrayOf(), 0)

        verify(inverse = true) { child.show() }
        verify(inverse = true) { child.hide() }
    }

    @Test
    fun testOnNestedPreScrollPassedMinusDyCase() {
        every { child.visibility }.returns(View.VISIBLE)

        hidingFloatingActionButtonBehavior.onNestedPreScroll(mockk(), child, mockk(), 0, -1, intArrayOf(), 0)

        verify(inverse = true) { child.show() }
        verify(inverse = true) { child.hide() }
    }
}