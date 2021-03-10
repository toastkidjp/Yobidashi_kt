/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser

import org.junit.Assert.assertSame
import org.junit.Test

class ScreenModeTest {

    @Test
    fun testFindWithEmpty() {
        assertSame(ScreenMode.EXPANDABLE, ScreenMode.find(""))
    }

    @Test
    fun testFindWithCorrectArgument() {
        assertSame(ScreenMode.FULL_SCREEN, ScreenMode.find("FULL_SCREEN"))
    }

    @Test
    fun testFindWithNoneTypa() {
        assertSame(ScreenMode.EXPANDABLE, ScreenMode.find("HALF_SCREEN"))
    }

}