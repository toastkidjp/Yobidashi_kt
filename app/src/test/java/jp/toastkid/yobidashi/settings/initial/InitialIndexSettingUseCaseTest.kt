/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.initial

import android.os.Bundle
import androidx.fragment.app.Fragment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.search.SearchFragment
import org.junit.After
import org.junit.Before
import org.junit.Test

class InitialIndexSettingUseCaseTest {

    private lateinit var initialIndexSettingUseCase: InitialIndexSettingUseCase

    @MockK
    private lateinit var argument: Bundle

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        initialIndexSettingUseCase = InitialIndexSettingUseCase()

        every { argument.putInt(any(), any()) }.answers { Unit }
    }

    @Test
    fun testNormalFragment() {
        initialIndexSettingUseCase.put(argument, Fragment::class.java)

        verify(exactly = 1) { argument.putInt(any(), 0) }
    }

    @Test
    fun testSearchFragment() {
        @Suppress("UNCHECKED_CAST")
        initialIndexSettingUseCase.put(argument, SearchFragment::class.java as Class<Fragment>)

        verify(exactly = 1) { argument.putInt(any(), 2) }
    }

    @Test
    fun testEditorFragment() {
        @Suppress("UNCHECKED_CAST")
        initialIndexSettingUseCase.put(argument, EditorFragment::class.java as Class<Fragment>)

        verify(exactly = 1) { argument.putInt(any(), 4) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}