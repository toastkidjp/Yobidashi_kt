/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClearSettingConfirmDialogFragmentTest {

    @MockK
    private lateinit var transaction: FragmentTransaction

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { transaction.add(any<Fragment>(), any()) }.returns(transaction)
        every { transaction.commit() }.returns(1)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        ClearSettingConfirmDialogFragment().show(transaction, "test")
    }

}