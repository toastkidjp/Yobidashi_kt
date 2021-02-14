/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.app.ActivityManager
import android.content.res.Resources
import android.graphics.Color
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class RecentAppColoringUseCaseTest {

    private lateinit var recentAppColoringUseCase: RecentAppColoringUseCase

    @MockK
    private lateinit var getString: (Int) -> String

    @MockK
    private lateinit var resourcesSupplier: () -> Resources

    @MockK
    private lateinit var setTaskDescription: (ActivityManager.TaskDescription) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { getString(any()) }.returns("test")
        every { resourcesSupplier.invoke() }.returns(mockk())
        every { setTaskDescription.invoke(any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testUnderLollipop() {
        recentAppColoringUseCase =
                RecentAppColoringUseCase(getString, resourcesSupplier, setTaskDescription, 20)

        recentAppColoringUseCase.invoke(Color.WHITE)

        verify(exactly = 0) { getString(any()) }
        verify(exactly = 0) { resourcesSupplier.invoke() }
        verify(exactly = 0) { setTaskDescription.invoke(any()) }
    }

    @Test
    fun testLollipop() {
        recentAppColoringUseCase =
                RecentAppColoringUseCase(getString, resourcesSupplier, setTaskDescription, 21)

        recentAppColoringUseCase.invoke(Color.WHITE)

        verify(exactly = 1) { getString(any()) }
        verify(exactly = 1) { resourcesSupplier.invoke() }
        verify(exactly = 1) { setTaskDescription.invoke(any()) }
    }

    @Test
    fun testQ() {
        recentAppColoringUseCase =
                RecentAppColoringUseCase(getString, resourcesSupplier, setTaskDescription, 29)

        recentAppColoringUseCase.invoke(Color.WHITE)

        verify(exactly = 1) { getString(any()) }
        verify(exactly = 0) { resourcesSupplier.invoke() }
        verify(exactly = 1) { setTaskDescription.invoke(any()) }
    }

}