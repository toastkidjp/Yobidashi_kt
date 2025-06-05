/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.usecase

import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.LoanPayment
import jp.toastkid.loan.model.PaymentDetail
import kotlin.math.max
import kotlin.math.pow

class LoanCalculator {

    operator fun invoke(factor: Factor) : LoanPayment {
        val paymentCount = (factor.term * 12).toDouble()
        val convertedRate = factor.interestRate / 100.0
        val actualTotalAmount = factor.amount - factor.downPayment
        var currentAmount = actualTotalAmount

        val poweredMonthlyInterestRate = (1 + convertedRate / 12).pow(paymentCount)

        val numerator = (actualTotalAmount * convertedRate) / 12 * poweredMonthlyInterestRate
        val denominator = poweredMonthlyInterestRate - 1
        val monthlyPaymentToBank = max(numerator / denominator, 0.0)

        val monthlyPayment = monthlyPaymentToBank.toLong() + factor.managementFee + factor.renovationReserves
        return LoanPayment(monthlyPayment, (0 until paymentCount.toInt()).map {
            val monthlyInterest = currentAmount * (convertedRate / 12)
            val monthlyActualReturning = monthlyPaymentToBank - monthlyInterest
            currentAmount -= (monthlyPaymentToBank - monthlyInterest).toLong()
            PaymentDetail(monthlyActualReturning, monthlyInterest, currentAmount)
        })
    }

}