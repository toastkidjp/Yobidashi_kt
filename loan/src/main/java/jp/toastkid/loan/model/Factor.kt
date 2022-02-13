/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.loan.model

class Factor(
    val amount: Int,
    val term: Int,
    val interestRate: Double,
    val downPayment: Int,
    val managementFee: Int,
    val renovationReserves: Int
)