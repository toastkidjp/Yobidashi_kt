/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

class TableSortService {

    operator fun invoke(
        lastSortOrder: Boolean,
        index: Int,
        snapshot: List<List<Any>>
    ): List<List<Any>>? {
        val first = snapshot.firstOrNull() ?: return null
        val swap = if (lastSortOrder)
            if (first[index].toString().toDoubleOrNull() != null) {
                snapshot.sortedBy { it[index].toString().toDoubleOrNull() ?: 0.0 }
            } else {
                snapshot.sortedBy { it[index].toString() }
            }
        else
            if (first[index].toString().toDoubleOrNull() != null) {
                snapshot.sortedByDescending { it[index].toString().toDoubleOrNull() ?: 0.0 }
            } else {
                snapshot.sortedByDescending { it[index].toString() }
            }

        return swap
    }

}