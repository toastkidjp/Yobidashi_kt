/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.service.uk

class EasterDateCalculator {

    operator fun invoke(year: Int): Pair<Int, Int> {
        val a = year.mod(19)
        val b = year.mod(4)
        val c = year.mod(7)
        val k = year.div(100)
        val p = (13 + 8 * k).div(25)
        val q = (k.div(4))
        val m = (15 - p + k - q).mod(30)
        val n = (4 + k - q).mod(7)
        val d = (19*a + m).mod(30)
        val e = (2*b + 4 * c + 6 * d + n).mod(7)
        val march = 22 + d + e
        val april = d + e - 9
        return march to april
    }

}