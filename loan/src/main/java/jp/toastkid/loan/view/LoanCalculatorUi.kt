/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.loan.R
import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.usecase.DebouncedCalculatorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun LoanCalculatorUi() {
    val context = LocalContext.current
    var result by rememberSaveable {
        mutableStateOf("")
    }
    var loanAmount by remember {
        mutableStateOf(context.getString(R.string.default_value_loan_amount))
    }
    var loanTerm by remember {
        mutableStateOf(context.getString(R.string.default_value_loan_term))
    }
    var interestRate by remember {
        mutableStateOf(context.getString(R.string.default_value_interest_rate))
    }
    var downPayment by remember {
        mutableStateOf(context.getString(R.string.default_value_down_payment))
    }
    var managementFee by remember {
        mutableStateOf("10,000")
    }
    var renovationReserves by remember {
        mutableStateOf("10,000")
    }

    val inputChannel: Channel<String> = Channel()

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
            result = context.getString(R.string.message_result_montly_payment, it)
        }
    ).invoke()

    Surface(elevation = 4.dp) {
        Column(
            Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = result, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = loanAmount,
                onValueChange = {
                    loanAmount = format(it)
                    onChange(inputChannel, it)
                },
                label = { Text(text = stringResource(R.string.hint_loan_amount)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = loanTerm,
                onValueChange = {
                    loanTerm = format(it)
                    onChange(inputChannel, it)
                },
                label = { Text(text = stringResource(R.string.hint_loan_term)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = interestRate,
                onValueChange = {
                    interestRate = it
                    onChange(inputChannel, it)
                },
                label = { Text(text = stringResource(R.string.hint_interest_rate)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = downPayment,
                onValueChange = {
                    downPayment = format(it)
                    onChange(inputChannel, it)
                },
                label = { Text(text = stringResource(R.string.hint_down_payment)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = managementFee,
                onValueChange = {
                    managementFee = format(it)
                    onChange(inputChannel, it)
                },
                label = { Text(text = "Management fee (Monthly)") },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = renovationReserves,
                onValueChange = {
                    renovationReserves = format(it)
                    onChange(inputChannel, it)
                },
                label = { Text(text = "Renovation reserves (Monthly)") },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun onChange(inputChannel: Channel<String>, text: String) {
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
