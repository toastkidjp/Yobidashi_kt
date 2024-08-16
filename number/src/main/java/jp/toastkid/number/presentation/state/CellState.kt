/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class CellState(
    val number: Int = -1,
    val open: Boolean = false
) {

    fun text() = if (number == -1) "_" else "$number"

}