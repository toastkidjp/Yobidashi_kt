/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.filter.color

import android.graphics.Color
import android.widget.FrameLayout
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ForegroundColorFilterUseCaseTest {

    @InjectMockKs
    private lateinit var foregroundColorFilterUseCase: ForegroundColorFilterUseCase

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var frameLayout: FrameLayout

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { preferenceApplier.useColorFilter() }.returns(true)
        every { preferenceApplier.filterColor(any()) }.returns(Color.YELLOW)
        every { frameLayout.setForeground(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        foregroundColorFilterUseCase.invoke(frameLayout)

        verify(exactly = 1) { preferenceApplier.useColorFilter() }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { frameLayout.setForeground(any()) }
    }

}