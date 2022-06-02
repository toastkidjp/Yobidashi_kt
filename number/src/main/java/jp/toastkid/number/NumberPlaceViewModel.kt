/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import jp.toastkid.number.model.NumberBoard
import jp.toastkid.number.model.NumberPlaceGame

class NumberPlaceViewModel : ViewModel() {

    private val _game = mutableStateOf(NumberPlaceGame())
    private val _mask = mutableStateOf(NumberBoard())

    fun initialize(maskingCount: Int) {
        _game.value.initialize(maskingCount)
        _mask.value = _game.value.masked()
    }

    fun initializeSolving() {
        _game.value.initializeSolving()
        _mask.value = _game.value.masked()
    }

    fun masked() = _mask.value

    fun place(rowIndex: Int, columnIndex: Int, it: Int, onSolved: (Boolean) -> Unit) {
        _game.value.place(rowIndex, columnIndex, it, onSolved)
    }

    fun useHint(
        rowIndex: Int,
        columnIndex: Int,
        numberState: MutableState<String>,
        onSolved: (Boolean) -> Unit
    ) {
        val it = _game.value.pickCorrect(rowIndex, columnIndex)
        numberState.value = "$it"
        _game.value.place(rowIndex, columnIndex, it, onSolved)
    }

    fun setGame(game: NumberPlaceGame) {
        _game.value = game
    }

}