/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.image

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class BitmapScalingTest {

    private lateinit var bitmapScaling: BitmapScaling

    @Before
    fun setUp() {
        bitmapScaling = BitmapScaling()
    }

    @Test
    fun test_bigger() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8)
        val scaled = bitmapScaling(bitmap, 300.0, 300.0)
        assertEquals(200, scaled.width)
        assertEquals(200, scaled.height)
    }

    @Test
    fun test_smaller() {
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ALPHA_8)
        val scaled = bitmapScaling(bitmap, 200.0, 200.0)
        assertEquals(200, scaled.width)
        assertEquals(200, scaled.height)
    }
}