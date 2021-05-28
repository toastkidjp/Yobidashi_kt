/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import android.content.Context
import android.graphics.Color
import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.yobidashi.libs.Toaster
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class BackgroundTabOpenerUseCaseTest {

    @InjectMockKs
    private lateinit var backgroundTabOpenerUseCase: BackgroundTabOpenerUseCase

    @MockK
    private lateinit var parent: View

    @MockK
    private lateinit var openBackgroundTab: (String, String) -> () -> Unit

    @MockK
    private lateinit var replaceToCurrentTab: () -> Unit

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { parent.getContext() }.returns(context)
        every { context.getString(any()) }.returns("test")
        every { context.getString(any(), any()) }.returns("test")
        every { openBackgroundTab.invoke(any(), any()) }.returns {  }
        every { replaceToCurrentTab.invoke() }.returns(Unit)

        mockkObject(Toaster)
        every { Toaster.withAction(any(), any(), any<String>(), any(), any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        backgroundTabOpenerUseCase.invoke(
                "test",
                "https://www.yahoo.co.jp",
                ColorPair(Color.BLACK, Color.WHITE)
        )

        verify(exactly = 1) { parent.getContext() }
        verify(exactly = 1) { context.getString(any(), any()) }
        verify(exactly = 1) { openBackgroundTab.invoke("test", "https://www.yahoo.co.jp") }
        verify(exactly = 1) { Toaster.withAction(any(), any(), any<String>(), any(), any(), any()) }
    }

}