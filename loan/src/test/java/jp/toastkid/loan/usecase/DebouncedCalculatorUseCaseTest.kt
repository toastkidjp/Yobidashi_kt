/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.usecase

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.LoanPayment
import jp.toastkid.loan.model.calculator.LoanPaymentCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DebouncedCalculatorUseCaseTest {

    @InjectMockKs
    private lateinit var debouncedCalculatorUseCase: DebouncedCalculatorUseCase

    private val inputChannel: Channel<String> = Channel()

    private val calculatorFlow = MutableStateFlow<LoanPaymentCalculator>(mockk())

    @MockK
    private lateinit var currentFactorProvider: () -> Factor

    @MockK
    private lateinit var onResult: (LoanPayment) -> Unit

    @Suppress("unused")
    private val debounceMillis = 0L

    @MockK
    private lateinit var calculator: LevelPaymentCalculator

    @Suppress("unused")
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { currentFactorProvider() }
            .returns(Factor(10000000, 35, 1.0, 100000, 10000, 10000))
        coEvery { calculator.invoke(any()) }.returns(LoanPayment(10000, emptyList()))
        coEvery { onResult(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @FlowPreview
    @Test
    fun testInvoke() {
        debouncedCalculatorUseCase.invoke()

        val countDownLatch = CountDownLatch(1)
        every { onResult(any()) } answers {
            countDownLatch.countDown()
        }

        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            calculatorFlow.value = (calculator)
            inputChannel.send("test")
        }

        countDownLatch.await(5, TimeUnit.SECONDS)
        job.cancel()

        verify { currentFactorProvider() }
        verify { calculator.invoke(any()) }
        verify { onResult(any()) }
    }

}