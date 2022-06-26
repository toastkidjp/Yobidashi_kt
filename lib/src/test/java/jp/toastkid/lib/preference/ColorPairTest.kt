/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.preference

import android.graphics.Color
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ColorPairTest {

    private lateinit var colorPair: ColorPair

    @Before
    fun setUp() {
        colorPair = ColorPair(Color.BLACK, Color.WHITE)
    }

    @Test
    fun bgColor() {
        assertEquals(Color.BLACK, colorPair.bgColor())
    }

    @Test
    fun fontColor() {
        assertEquals(Color.WHITE, colorPair.fontColor())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}