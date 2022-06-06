/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.number.factory.GameFileProvider
import jp.toastkid.number.model.NumberBoard
import jp.toastkid.number.model.NumberPlaceGame
import jp.toastkid.number.repository.GameRepositoryImplementation

class NumberPlaceViewModel : ViewModel() {

    private val _game = mutableStateOf(NumberPlaceGame())
    private val _mask = mutableStateOf(NumberBoard())
    private val _loading = mutableStateOf(false)

    fun initialize(maskingCount: Int) {
        _loading.value = true
        _game.value.initialize(maskingCount)
        _mask.value = _game.value.masked()
        _loading.value = false
    }

    fun initializeSolving() {
        _loading.value = true
        _game.value.initializeSolving()
        _mask.value = _game.value.masked()
        _loading.value = false
    }

    fun setGame(game: NumberPlaceGame) {
        _loading.value = true
        _game.value = game
        _mask.value = _game.value.masked()
        _loading.value = false
    }

    fun masked() = _mask.value

    fun loading(): State<Boolean> = _loading

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

    fun saveCurrentGame(context: Context) {
        val preferenceApplier = PreferenceApplier(context)
        val file = GameFileProvider().invoke(context.filesDir, preferenceApplier) ?: return
        GameRepositoryImplementation().save(file, _game.value)
    }

    fun pickSolving(rowIndex: Int, columnIndex: Int): Int {
        return _game.value.pickSolving(rowIndex, columnIndex)
    }

}