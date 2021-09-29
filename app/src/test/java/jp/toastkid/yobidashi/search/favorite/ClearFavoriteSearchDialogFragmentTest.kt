/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.favorite

import androidx.fragment.app.FragmentManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClearFavoriteSearchDialogFragmentTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(ClearFavoriteSearchDialogFragment::class)
        every { anyConstructed<ClearFavoriteSearchDialogFragment>().arguments = any() }.answers { Unit }
        every { anyConstructed<ClearFavoriteSearchDialogFragment>().show(any<FragmentManager>(), any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        ClearFavoriteSearchDialogFragment.show(mockk(), mockk())

        verify(exactly = 1) { anyConstructed<ClearFavoriteSearchDialogFragment>().arguments = any() }
        verify(exactly = 1) { anyConstructed<ClearFavoriteSearchDialogFragment>().show(any<FragmentManager>(), any())  }
    }

}