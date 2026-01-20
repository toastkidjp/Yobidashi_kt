/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.scroll.StateScrollerFactory
import jp.toastkid.loan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanCalculatorUi() {
    val context = LocalContext.current

    val viewModel = remember { LoanCalculatorViewModel() }

    Surface(shadowElevation = 4.dp) {
        LazyColumn(state = viewModel.scrollState(),
            modifier = Modifier
                .padding(8.dp)) {
            item {
                Column {
                    Text(
                        text = viewModel.result(),
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.loanAmount(),
                        onValueChange = {
                            viewModel.updateLoanAmount(it)
                        },
                        label = { Text(text = stringResource(R.string.hint_loan_amount)) },
                        colors = makeTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row {
                        OutlinedTextField(
                            value = viewModel.loanTerm(),
                            onValueChange = {
                                viewModel.updateLoanTerm(it)
                            },
                            label = { Text(text = stringResource(R.string.hint_loan_term)) },
                            colors = makeTextFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = viewModel.interestRate(),
                            onValueChange = {
                                viewModel.updateInterestRate(it)
                            },
                            label = { Text(text = stringResource(R.string.hint_interest_rate)) },
                            colors = makeTextFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = viewModel.downPayment(),
                        onValueChange = {
                            viewModel.updateDownPayment(it)
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(jp.toastkid.lib.R.drawable.ic_clear_form),
                                contentDescription = stringResource(jp.toastkid.lib.R.string.reset),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    viewModel.clearDownPayment()
                                }
                            )
                        },
                        label = { Text(text = stringResource(R.string.hint_down_payment)) },
                        colors = makeTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.managementFee(),
                        onValueChange = {
                            viewModel.updateManagementFee(it)
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(jp.toastkid.lib.R.drawable.ic_clear_form),
                                contentDescription = stringResource(jp.toastkid.lib.R.string.reset),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    viewModel.clearManagementFee()
                                }
                            )
                        },
                        label = { Text(text = "Management fee (Monthly)") },
                        colors = makeTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.renovationReserves(),
                        onValueChange = {
                            viewModel.updateRenovationReserves(it)
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(jp.toastkid.lib.R.drawable.ic_clear_form),
                                contentDescription = stringResource(jp.toastkid.lib.R.string.reset),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    viewModel.clearRenovationReserves()
                                }
                            )
                        },
                        label = { Text(text = "Renovation reserves (Monthly)") },
                        colors = makeTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            stickyHeader {
                val surfaceColor = MaterialTheme.colorScheme.surface
                val backgroundColor =
                    derivedStateOf {
                        if (viewModel.scrollState().firstVisibleItemIndex != 0) { surfaceColor }
                        else Color.Transparent
                    }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateItem()
                        .drawBehind { drawRect(backgroundColor.value) }
                ) {
                    Text(
                        stringResource(R.string.title_column_loan_payment_count),
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        stringResource(R.string.title_column_principal),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.title_column_interest),
                        modifier = Modifier.weight(1f)
                    )
                    Text(stringResource(R.string.title_column_balance), modifier = Modifier.weight(1f))
                }
            }
            itemsIndexed(viewModel.scheduleState()) { index, it ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateItem()
                ) {
                    Text(
                        "${(index / 12) + 1} ${(index % 12) + 1}(${index + 1})",
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(viewModel.roundToIntSafely(it.principal), modifier = Modifier.weight(1f))
                    Text(viewModel.roundToIntSafely(it.interest), modifier = Modifier.weight(1f))
                    Text(it.amount.toString(), modifier = Modifier.weight(1f))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.launch { monthlyPayment, sum ->
            context.getString(
                R.string.message_result_montly_payment,
                monthlyPayment,
                sum
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        withContext(Dispatchers.IO) {
            (context as? ComponentActivity)
                ?.let { ViewModelProvider(it).get(ContentViewModel::class) }
                ?.receiveEvent(StateScrollerFactory().invoke(viewModel.scrollState()))
        }
    }
}

@Composable
private fun makeTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    unfocusedContainerColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.onSurface
)
