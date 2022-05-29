/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.number

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NumberPlaceViewModel : ViewModel() {

    private val _correct = mutableStateOf(NumberBoard())

    private val _masked = mutableStateOf(NumberBoard())

    private val _solving = mutableStateOf(NumberBoard())

    init {
        _correct.value = getValidBoard()
        _masked.value = _correct.value.masked()
        _solving.value.copyFrom(_masked.value)
    }

    fun masked() = _masked.value

    private fun getValidBoard(): NumberBoard {
        val board = NumberBoard()
        repeat(50000) {
            board.placeRandom()
            if (board.validBoard()) {
                return board
            }
        }
        return board
    }

    fun place(rowIndex: Int, columnIndex: Int, it: Int, onSolved: (Boolean) -> Unit) {
        _solving.value.place(rowIndex, columnIndex, it)
        if (_solving.value.fulfilled().not()) {
            return
        }

        onSolved(_solving.value.isCorrect(_correct.value))
    }

    fun useHint(rowIndex: Int, columnIndex: Int, numberState: MutableState<String>, onSolved: (Boolean) -> Unit) {
        val it = _correct.value.pick(rowIndex, columnIndex)
        numberState.value = "$it"
        place(rowIndex, columnIndex, it, onSolved)
    }

}