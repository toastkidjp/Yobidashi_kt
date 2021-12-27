/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.loan.Calculator
import jp.toastkid.loan.R
import jp.toastkid.loan.databinding.FragmentLoanCalculatorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoanCalculatorFragment : Fragment() {

    private var binding: FragmentLoanCalculatorBinding? = null

    private val inputChannel: Channel<String> = Channel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loan_calculator, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    inputChannel.send(s?.toString() ?: "")
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit

        }

        binding?.loanAmount?.addTextChangedListener(textWatcher)
        binding?.term?.addTextChangedListener(textWatcher)
        binding?.interestRate?.addTextChangedListener(textWatcher)
        binding?.downPayment?.addTextChangedListener(textWatcher)
        binding?.monthlyManagementFee?.addTextChangedListener(textWatcher)
        binding?.monthlyRenovationReserves?.addTextChangedListener(textWatcher)

        CoroutineScope(Dispatchers.IO).launch {
            val calculator = Calculator()

            inputChannel
                .receiveAsFlow()
                .distinctUntilChanged()
                .debounce(1000)
                .flowOn(Dispatchers.Main)
                .collect {
                    val payment = calculator(
                        extractInt(binding?.loanAmount),
                        extractInt(binding?.term),
                        extractDouble(binding?.interestRate),
                        extractInt(binding?.downPayment),
                        extractInt(binding?.monthlyManagementFee),
                        extractInt(binding?.monthlyRenovationReserves),
                    )

                    binding?.result?.text = "Monthly payment: $payment"
                }
        }
    }

    private fun extractInt(editText: EditText?) =
        editText?.text?.toString()?.toIntOrNull() ?: 0

    private fun extractDouble(editText: EditText?) =
        editText?.text?.toString()?.toDoubleOrNull() ?: 0.0

    override fun onDetach() {
        binding = null
        super.onDetach()
    }

}