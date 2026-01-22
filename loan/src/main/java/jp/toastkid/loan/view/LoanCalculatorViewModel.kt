/*
 * Copyright (c) 2025 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.loan.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.PaymentDetail
import jp.toastkid.loan.usecase.DebouncedCalculatorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.roundToInt

class LoanCalculatorViewModel {

    private val result = mutableStateOf("")

    fun result() = result.value

    private val loanAmount = TextFieldState("35,000,000")

    fun loanAmount() = loanAmount

    private val loanTerm = TextFieldState("35")

    fun loanTerm() = loanTerm

    private val interestRate = TextFieldState("1.0")

    fun interestRate() = interestRate

    private val downPayment = TextFieldState("1,000,000")

    fun downPayment() = downPayment

    fun clearDownPayment() {
        downPayment.clearText()
    }

    private val managementFee = TextFieldState("10,000")

    fun managementFee() = managementFee

    fun clearManagementFee() {
        managementFee.clearText()
    }

    private val renovationReserves = TextFieldState("10,000")

    fun renovationReserves() = renovationReserves

    fun clearRenovationReserves() {
        renovationReserves.clearText()
    }

    private fun updateValue(
        newValue: TextFieldValue,
    ) {
        val newText = format(newValue.text)
        onChange(newText)
    }

    private val scheduleState = mutableStateListOf<PaymentDetail>()

    fun scheduleState() = scheduleState

    private val inputChannel: Channel<String> = Channel()

    private val scrollState = LazyListState()

    fun scrollState() = scrollState

    fun onChange(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            inputChannel.send(text)
        }
    }

    fun roundToIntSafely(d: Double): String =
        if (d.isNaN()) "0" else DECIMAL_FORMAT.format(d.roundToInt())

    private fun format(input: String?): String {
        if (input.isNullOrBlank()) {
            return "0"
        }

        return DECIMAL_FORMAT.format(
            input.filter { it.isDigit() || it == '.' }.trim().toBigDecimalOrNull()
        )
    }

    private fun extractLong(editText: String) =
        editText.replace(",", "").toLongOrNull() ?: 0

    private fun extractInt(editText: String) =
        editText.replace(",", "").toIntOrNull() ?: 0

    private fun extractDouble(editText: String) =
        editText.replace(",", "").toDoubleOrNull() ?: 0.0

    fun launch(stringFormatter: (Long, Long) -> String) {
        DebouncedCalculatorUseCase(
            inputChannel,
            {
                Factor(
                    extractLong(loanAmount.text.toString()),
                    extractInt(loanTerm.text.toString()),
                    extractDouble(interestRate.text.toString()),
                    extractInt(downPayment.text.toString()),
                    extractInt(managementFee.text.toString()),
                    extractInt(renovationReserves.text.toString())
                )
            },
            {
                result.value = stringFormatter(
                    it.monthlyPayment,
                    it.paymentSchedule.sumOf(PaymentDetail::interest).toLong()
                )

                scheduleState.clear()
                scheduleState.addAll(it.paymentSchedule)
            }
        ).invoke()
    }

}

private val DECIMAL_FORMAT = DecimalFormat("#,###.##")
