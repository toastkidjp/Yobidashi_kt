/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.model

data class NumberPlaceGame(
    private val correct: NumberBoard = NumberBoard(),
    private val masked: NumberBoard = NumberBoard(),
    private val solving: NumberBoard = NumberBoard()
) {

    fun initialize(maskingCount: Int) {
        getValidBoard(correct)
        masked.copyFrom(correct.masked(maskingCount))
        initializeSolving()
    }

    fun initializeSolving() {
        solving.copyFrom(masked)
    }

    fun masked() = masked

    private fun getValidBoard(board: NumberBoard) {
        repeat(50000) {
            board.placeRandom()
            if (board.validBoard()) {
                return
            }
            board.fillZero()
        }
        return
    }

    fun place(rowIndex: Int, columnIndex: Int, it: Int, onSolved: (Boolean) -> Unit) {
        solving.place(rowIndex, columnIndex, it)
        if (solving.fulfilled().not()) {
            return
        }

        onSolved(solving.isCorrect(correct))
    }

    fun pickCorrect(rowIndex: Int, columnIndex: Int): Int {
        return correct.pick(rowIndex, columnIndex)
    }

}