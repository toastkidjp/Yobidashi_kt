/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.loan.model.calculator

import jp.toastkid.loan.model.Factor
import jp.toastkid.loan.model.LoanPayment

interface LoanPaymentCalculator {
    operator fun invoke(factor: Factor): LoanPayment
}
