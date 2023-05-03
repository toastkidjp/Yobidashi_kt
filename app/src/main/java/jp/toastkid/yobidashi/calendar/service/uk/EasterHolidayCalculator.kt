/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service.uk

import jp.toastkid.yobidashi.calendar.model.holiday.Holiday

class EasterHolidayCalculator(
    private val easterDateCalculator: EasterDateCalculator = EasterDateCalculator()
) {

    operator fun invoke(year: Int, month: Int): List<Holiday> {
        val (march, _) = easterDateCalculator.invoke(year)
        val goodFridayDate = march - 2
        val easterMondayDate = march + 1
        return listOf(
            Holiday(
                "Good Friday",
                if (inMarch(goodFridayDate)) 3 else 4,
                if (inMarch(goodFridayDate)) goodFridayDate else goodFridayDate - 31,
                "\uD83C\uDDEC\uD83C\uDDE7"
            ),
            Holiday(
                "Easter Monday",
                if (inMarch(easterMondayDate)) 3 else 4,
                if (inMarch(easterMondayDate)) easterMondayDate else easterMondayDate - 31,
                "\uD83C\uDDEC\uD83C\uDDE7"
            )
        ).filter { it.month == month }
    }

    private fun inMarch(march: Int) = march in 1..31
}