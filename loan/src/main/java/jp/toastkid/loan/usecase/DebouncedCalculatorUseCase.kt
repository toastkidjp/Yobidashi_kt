/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.usecase

import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.LoanPayment
import jp.toastkid.loan.model.calculator.LoanPaymentCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DebouncedCalculatorUseCase(
    private val inputChannel: Channel<String>,
    private val currentFactorProvider: () -> Factor,
    private val onResult: (LoanPayment) -> Unit,
    private val calculatorFlow: Flow<LoanPaymentCalculator>,
    private val debounceMillis: Long = 1000,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    @FlowPreview
    operator fun invoke() {
        CoroutineScope(ioDispatcher).launch {
            combine(
                inputChannel
                    .receiveAsFlow()
                    .distinctUntilChanged(),
                calculatorFlow
                    .distinctUntilChanged()
            ) { _, calculator ->
                val factor = currentFactorProvider()
                val payment = calculator(factor)

                payment
            }
                .debounce(debounceMillis)
                .collect { onResult(it) }
        }
    }

}