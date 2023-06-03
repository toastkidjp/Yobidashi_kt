/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.vietnam

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class VietnamLunarDateConverterTest {

    @Test
    fun test() {
        val tet2021 = VietnamLunarDateConverter().lunarDateToSolar(2021, 1, 1)
        assertEquals(2021, tet2021.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, tet2021.get(Calendar.MONTH))
        assertEquals(12, tet2021.get(Calendar.DAY_OF_MONTH))

        val tet2023 = VietnamLunarDateConverter().lunarDateToSolar(2023, 1, 1)
        assertEquals(2023, tet2023.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, tet2023.get(Calendar.MONTH))
        assertEquals(22, tet2023.get(Calendar.DAY_OF_MONTH))

        val hungKingsCommemorationsDay2023 = VietnamLunarDateConverter().lunarDateToSolar(2023, 3, 10)
        assertEquals(2023, hungKingsCommemorationsDay2023.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, hungKingsCommemorationsDay2023.get(Calendar.MONTH))
        assertEquals(29, hungKingsCommemorationsDay2023.get(Calendar.DAY_OF_MONTH))
    }

}