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
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
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

class BottomNavigationBehaviorTest {

    @InjectMockKs
    private lateinit var bottomNavigationBehavior: BottomNavigationBehavior

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var attrs: AttributeSet

    @MockK
    private lateinit var typedArray: TypedArray

    @MockK
    private lateinit var child: BottomAppBar

    @MockK
    private lateinit var snackbarLayout: Snackbar.SnackbarLayout

    @MockK
    private lateinit var layoutParams: CoordinatorLayout.LayoutParams

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.obtainStyledAttributes(any<AttributeSet>(), any()) }.returns(typedArray)
        every { typedArray.getBoolean(any(), any()) }.returns(true)
        every { typedArray.recycle() }.just(Runs)

        bottomNavigationBehavior = BottomNavigationBehavior(context, attrs)

        every { child.translationY = any() }.just(Runs)
        every { child.translationY }.returns(10f)
        every { child.height }.returns(56)
        every { child.id }.returns(1)
        every { snackbarLayout.layoutParams }.returns(layoutParams)
        every { snackbarLayout.layoutParams = any() }.just(Runs)
        every { layoutParams.anchorId = any() }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOnNestedPreScroll() {
        bottomNavigationBehavior.onNestedPreScroll(mockk(), child, mockk(), 0, 10, intArrayOf(), 0)

        verify { child.translationY = any() }
        verify { child.translationY }
        verify { child.height }
    }

    @Test
    fun testLayoutDependsOn() {
        bottomNavigationBehavior.layoutDependsOn(mockk(), child, snackbarLayout)

        verify { child.id }
        verify { snackbarLayout.layoutParams }
        verify { snackbarLayout.layoutParams = any() }
        verify { layoutParams.anchorId = any() }
    }

    @Test
    fun layoutDependsOn() {
        val dependency = mockk<View>()
        every { dependency.layoutParams }.returns(layoutParams)

        bottomNavigationBehavior.layoutDependsOn(mockk(), child, dependency)

        verify(inverse = true) { child.id }
        verify(inverse = true) { snackbarLayout.layoutParams }
        verify(inverse = true) { snackbarLayout.layoutParams = any() }
        verify(inverse = true) { layoutParams.anchorId = any() }
    }

}