/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number

import androidx.compose.ui.unit.sp
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class NumberPlaceViewModelTest {

    @InjectMockKs
    private lateinit var numberPlaceViewModel: NumberPlaceViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        numberPlaceViewModel.initialize(20)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun masked() {
        assertFalse(numberPlaceViewModel.masked().fulfilled())
    }

    @Test
    fun place() {
        val callback = mockk<(Boolean) -> Unit>()

        numberPlaceViewModel.place(2, 3,  4, callback)

        verify(inverse = true) { callback(any()) }
    }

    @Test
    fun numberLabel() {
        assertEquals("_", numberPlaceViewModel.numberLabel(-1))
        assertEquals("1", numberPlaceViewModel.numberLabel(1))
    }

    @Test
    fun fontSize() {
        assertEquals(32.sp, numberPlaceViewModel.fontSize())
    }

}