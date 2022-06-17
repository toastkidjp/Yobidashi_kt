/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class PreferencesClearUseCaseTest {

    @InjectMockKs
    private lateinit var preferencesClearUseCase: PreferencesClearUseCase

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var viewModelProvider: ViewModelProvider

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getSharedPreferences(any(), any()) }.returns(mockk())
        every { viewModelProvider.get(any<Class<ContentViewModel>>()) }
            .returns(contentViewModel)
        every { contentViewModel.snackShort(any<Int>()) }.returns(Unit)

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().clear() }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        preferencesClearUseCase.invoke()

        verify(exactly = 1) { context.getSharedPreferences(any(), any()) }
        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { anyConstructed<PreferenceApplier>().clear() }
    }

}