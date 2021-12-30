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
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import jp.toastkid.loan.Calculator
import jp.toastkid.loan.model.Factor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test

class DebouncedCalculatorUseCaseTest {

    @InjectMockKs
    private lateinit var debouncedCalculatorUseCase: DebouncedCalculatorUseCase

    private val inputChannel: Channel<String> = Channel()

    @MockK
    private lateinit var currentFactorProvider: () -> Factor

    @MockK
    private lateinit var onResult: (Int) -> Unit

    private val debounceMillis = 0L

    @MockK
    private lateinit var calculator: Calculator

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { currentFactorProvider() }
            .returns(Factor(10000000, 35, 1.0, 100000, 10000, 10000))
        coEvery { calculator.invoke(any()) }.returns(100)
        coEvery { onResult(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        debouncedCalculatorUseCase.invoke()

        CoroutineScope(Dispatchers.Unconfined).launch {
            inputChannel.send("test")

            coVerify { currentFactorProvider() }
            coVerify { calculator.invoke(any()) }
            coVerify { onResult(any()) }
        }
    }
}