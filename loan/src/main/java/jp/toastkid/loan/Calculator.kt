/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan

import kotlin.math.max
import kotlin.math.pow

class Calculator {

    operator fun invoke(
        amount: Int,
        term: Int,
        interestRate: Double,
        downPayment: Int,
        managementFee: Int,
        renovationReserves: Int
    ): Int {
        val paymentCount = (term * 12).toDouble()
        val convertedRate = interestRate / 100.0
        val poweredMonthlyInterestRate = (1 + convertedRate / 12).pow(paymentCount)

        val numerator = ((amount - downPayment) * convertedRate) / 12 * poweredMonthlyInterestRate
        val denominator = poweredMonthlyInterestRate - 1

        return (max(numerator / denominator, 0.0)).toInt() + managementFee + renovationReserves
    }

}