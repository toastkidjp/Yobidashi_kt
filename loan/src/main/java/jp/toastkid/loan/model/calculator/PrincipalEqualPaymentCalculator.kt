package jp.toastkid.loan.model.calculator

import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.LoanPayment
import jp.toastkid.loan.model.PaymentDetail
import kotlin.math.roundToLong

class PrincipalEqualPaymentCalculator : LoanPaymentCalculator {

    override operator fun invoke(factor: Factor): LoanPayment {
        val totalMonths = factor.term * 12
        val loanAmount = factor.amount - factor.downPayment

        val monthlyPrincipal = loanAmount.toDouble() / totalMonths
        val monthlyInterestRate = factor.interestRate / 100.0 / 12.0

        val schedule = mutableListOf<PaymentDetail>()
        var remainingbalance = loanAmount.toDouble()

        for (i in 1..totalMonths) {
            val interest = remainingbalance * monthlyInterestRate

            val totalAmount = (monthlyPrincipal + interest + factor.managementFee + factor.renovationReserves).roundToLong()

            schedule.add(
                PaymentDetail(
                    principal = monthlyPrincipal,
                    interest = interest,
                    amount = totalAmount
                )
            )

            remainingbalance -= monthlyPrincipal
        }

        return LoanPayment(
            monthlyPayment = schedule.firstOrNull()?.amount ?: -1L,
            paymentSchedule = schedule
        )
    }

}
