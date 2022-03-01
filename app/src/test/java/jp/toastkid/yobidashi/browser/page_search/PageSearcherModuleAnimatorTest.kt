/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.page_search

import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify

class PageSearcherModuleAnimatorTest {

    @InjectMockKs
    private lateinit var pageSearcherModuleAnimator: PageSearcherModuleAnimator

    @MockK
    private lateinit var view: View

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var animator: ViewPropertyAnimator

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { view.animate() }.returns(animator)
        every { animator.cancel() }.just(Runs)
        every { animator.translationY(any()) }.returns(animator)
        every { animator.setDuration(any()) }.returns(animator)
        every { animator.withStartAction(any()) }.returns(animator)
        every { animator.withEndAction(any()) }.returns(animator)
        every { animator.start() }.just(Runs)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun show() {
        pageSearcherModuleAnimator.show(view, editText)

        verify { animator.cancel() }
        verify { animator.start() }
    }
}