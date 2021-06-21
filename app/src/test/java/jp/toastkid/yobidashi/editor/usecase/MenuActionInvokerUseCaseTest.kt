/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.yobidashi.editor.ListHeadAdder
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class MenuActionInvokerUseCaseTest {

    @InjectMockKs
    private lateinit var menuActionInvokerUseCase: MenuActionInvokerUseCase

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var speechMaker: SpeechMaker

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @MockK
    private lateinit var listHeadAdder: ListHeadAdder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.context }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        assertFalse(menuActionInvokerUseCase.invoke(-1, ""))

        verify(exactly = 1) { editText.context }
    }
}