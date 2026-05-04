package jp.toastkid.loan.model.calculator

import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.LoanPayment
import jp.toastkid.loan.model.PaymentDetail
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToLong

class PrincipalEqualPaymentCalculator : LoanPaymentCalculator {

    override operator fun invoke(factor: Factor): LoanPayment {
        val totalMonths = factor.term * 12
        val loanAmount = factor.amount - factor.downPayment

        val monthlyPrincipal = loanAmount.toDouble() / totalMonths
        val monthlyInterestRate = factor.interestRate / 100.0 / 12.0

        val schedule = mutableListOf<PaymentDetail>()
        var remainingbalance = loanAmount.toDouble()
        val firstPayment = AtomicLong(-1)

        for (i in 1..totalMonths) {
            val interest = remainingbalance * monthlyInterestRate

            val totalAmount = (monthlyPrincipal + interest + factor.managementFee + factor.renovationReserves).roundToLong()

            remainingbalance -= monthlyPrincipal
            if (firstPayment.get() == -1L) {
                firstPayment.set(totalAmount)
            }

            schedule.add(
                PaymentDetail(
                    principal = totalAmount.toDouble(),
                    interest = interest,
                    amount = remainingbalance.toLong()
                )
            )
        }

        return LoanPayment(
            monthlyPayment = firstPayment.get(),
            paymentSchedule = schedule
        )
    }

}
