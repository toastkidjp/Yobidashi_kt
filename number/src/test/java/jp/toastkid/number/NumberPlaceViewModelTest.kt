/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.number

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.unmockkAll
import org.junit.Assert.assertFalse

class NumberPlaceViewModelTest {

    @InjectMockKs
    private lateinit var numberPlaceViewModel: NumberPlaceViewModel

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun masked() {
        assertFalse(numberPlaceViewModel.masked().fulfilled())
    }

}