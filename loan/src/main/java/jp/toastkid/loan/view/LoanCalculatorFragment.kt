/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import jp.toastkid.loan.R
import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.usecase.DebouncedCalculatorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class LoanCalculatorFragment : Fragment() {

    private val inputChannel: Channel<String> = Channel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoanCalculatorUi()
            }
        }
    }

    @Composable
    fun LoanCalculatorUi() {
        var result by rememberSaveable {
            mutableStateOf("")
        }
        var loanAmount by remember {
            mutableStateOf(getString(R.string.default_value_loan_amount))
        }
        var loanTerm by remember {
            mutableStateOf(getString(R.string.default_value_loan_term))
        }
        var interestRate by remember {
            mutableStateOf(getString(R.string.default_value_interest_rate))
        }
        var downPayment by remember {
            mutableStateOf(getString(R.string.default_value_down_payment))
        }
        var managementFee by remember {
            mutableStateOf("10,000")
        }
        var renovationReserves by remember {
            mutableStateOf("10,000")
        }

        DebouncedCalculatorUseCase(
            inputChannel,
            {
                Factor(
                    extractLong(loanAmount),
                    extractInt(loanTerm),
                    extractDouble(interestRate),
                    extractInt(downPayment),
                    extractInt(managementFee),
                    extractInt(renovationReserves)
                )
            },
            {
                result = getString(R.string.message_result_montly_payment, it)
            }
        ).invoke()

        Column(
            Modifier
                .background(color = Color(0xbbFFFFFF))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = result, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = loanAmount,
                onValueChange = {
                    loanAmount = format(it)
                    onChange(it)
                },
                label = { Text(text = stringResource(R.string.hint_loan_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = loanTerm,
                onValueChange = {
                    loanTerm = format(it)
                    onChange(it)
                },
                label = { Text(text = stringResource(R.string.hint_loan_term)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = interestRate,
                onValueChange = {
                    interestRate = it
                    onChange(it)
                },
                label = { Text(text = stringResource(R.string.hint_interest_rate)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = downPayment,
                onValueChange = {
                    downPayment = format(it)
                    onChange(it)
                },
                label = { Text(text = stringResource(R.string.hint_down_payment)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = managementFee,
                onValueChange = {
                    managementFee = format(it)
                    onChange(it)
                },
                label = { Text(text = "Management fee (Monthly)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = renovationReserves,
                onValueChange = {
                    renovationReserves = format(it)
                    onChange(it)
                },
                label = { Text(text = "Renovation reserves (Monthly)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    private fun onChange(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            inputChannel.send(text ?: "")
        }
    }

    private val formatter = DecimalFormat("#,###.##")

    private fun format(input: String?): String {
        if (input.isNullOrBlank()) {
            return "0"
        }

        return formatter.format(
            input.filter { it.isDigit() || it == '.' }.trim()?.toBigDecimalOrNull()
        )
    }

    private fun extractLong(editText: String) =
        editText.replace(",", "")?.toLongOrNull() ?: 0

    private fun extractInt(editText: String) =
        editText.replace(",", "")?.toIntOrNull() ?: 0

    private fun extractDouble(editText: String) =
        editText.replace(",", "")?.toDoubleOrNull() ?: 0.0

}