/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.planning

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CardFragmentAttachingUseCaseTest {

    @InjectMockKs
    private lateinit var cardFragmentAttachingUseCase: CardFragmentAttachingUseCase

    @MockK
    private lateinit var fragmentManager: FragmentManager

    @MockK
    private lateinit var transaction: FragmentTransaction

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { fragmentManager.beginTransaction() }.returns(transaction)
        every { transaction.setCustomAnimations(any(), any(), any(), any()) }.returns(transaction)
        every { transaction.add(any<Int>(), any()) }.returns(transaction)
        every { transaction.addToBackStack(any()) }.returns(transaction)
        every { transaction.commit() }.returns(0)

        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putCharSequence(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        cardFragmentAttachingUseCase.invoke("1")

        verify(exactly = 1) { fragmentManager.beginTransaction() }
        verify(exactly = 1) { transaction.setCustomAnimations(any(), any(), any(), any()) }
        verify(exactly = 1) { transaction.add(any<Int>(), any()) }
        verify(exactly = 1) { transaction.addToBackStack(any()) }
        verify(exactly = 1) { transaction.commit() }
    }

    @Test
    fun testTextIsBlank() {
        cardFragmentAttachingUseCase.invoke(" ")

        verify(exactly = 0) { fragmentManager.beginTransaction() }
        verify(exactly = 0) { transaction.setCustomAnimations(any(), any(), any(), any()) }
        verify(exactly = 0) { transaction.add(any<Int>(), any()) }
        verify(exactly = 0) { transaction.addToBackStack(any()) }
        verify(exactly = 0) { transaction.commit() }
    }

}