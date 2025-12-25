/*
 * Copyright (c) 2025 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.loan.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
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

    private val loanAmount = mutableStateOf(TextFieldValue("35,000,000"))

    fun loanAmount() = loanAmount.value

    fun updateLoanAmount(newValue: TextFieldValue) {
        updateValue(newValue, loanAmount)
    }

    private val loanTerm = mutableStateOf(TextFieldValue("35"))

    fun loanTerm() = loanTerm.value

    fun updateLoanTerm(newValue: TextFieldValue) {
        updateValue(newValue, loanTerm)
    }

    private val interestRate = mutableStateOf(TextFieldValue("1.0"))

    fun interestRate() = interestRate.value

    fun updateInterestRate(newValue: TextFieldValue) {
        updateValue(newValue, interestRate)
    }

    private val downPayment = mutableStateOf(TextFieldValue("1,000,000"))

    fun downPayment() = downPayment.value

    fun updateDownPayment(newValue: TextFieldValue) {
        updateValue(newValue, downPayment)
    }

    fun clearDownPayment() {
        updateValue(TextFieldValue("0"), downPayment)
    }

    private val managementFee = mutableStateOf(TextFieldValue("10,000"))

    fun managementFee() = managementFee.value

    fun updateManagementFee(newValue: TextFieldValue) {
        updateValue(newValue, managementFee)
    }

    fun clearManagementFee() {
        updateValue(TextFieldValue("0"), managementFee)
    }

    private val renovationReserves = mutableStateOf(TextFieldValue("10,000"))

    fun renovationReserves() = renovationReserves.value

    fun updateRenovationReserves(newValue: TextFieldValue) {
        updateValue(newValue, renovationReserves)
    }

    fun clearRenovationReserves() {
        updateValue(TextFieldValue("0"), renovationReserves)
    }

    private fun updateValue(
        newValue: TextFieldValue,
        state: MutableState<TextFieldValue>
    ) {
        val newText = format(newValue.text)
        state.value = newValue.copy(text = newText)
        onChange(newText)
    }

    private val scheduleState = mutableStateListOf<PaymentDetail>()

    fun scheduleState() = scheduleState

    private val inputChannel: Channel<String> = Channel()

    private val scrollState = LazyListState()

    fun scrollState() = scrollState

    private fun onChange(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            inputChannel.send(text)
        }
    }

    fun roundToIntSafely(d: Double) =
        if (d.isNaN()) "0" else DECIMAL_FORMAT.format(d.roundToInt())

    private fun format(input: String?): String {
        if (input.isNullOrBlank()) {
            return "0"
        }

        return DECIMAL_FORMAT.format(
            input.filter { it.isDigit() || it == '.' }.trim()?.toBigDecimalOrNull()
        )
    }

    private fun extractLong(editText: String) =
        editText.replace(",", "").toLongOrNull() ?: 0

    private fun extractInt(editText: String) =
        editText.replace(",", "").toIntOrNull() ?: 0

    private fun extractDouble(editText: String) =
        editText.replace(",", "")?.toDoubleOrNull() ?: 0.0

    fun launch(stringFormatter: (Long, Long) -> String) {
        DebouncedCalculatorUseCase(
            inputChannel,
            {
                Factor(
                    extractLong(loanAmount.value.text),
                    extractInt(loanTerm.value.text),
                    extractDouble(interestRate.value.text),
                    extractInt(downPayment.value.text),
                    extractInt(managementFee.value.text),
                    extractInt(renovationReserves.value.text)
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
