/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.uk

import org.junit.Assert.assertEquals
import org.junit.Test

class EasterDateCalculatorTest {

    @Test
    fun test() {
        val cal = EasterDateCalculator()
        val easterCandidates = (2022..2024).map { cal.invoke(it) }
        assertEquals(17, easterCandidates[0].second)
        assertEquals(9, easterCandidates[1].second)
        assertEquals(31, easterCandidates[2].first)
    }

}