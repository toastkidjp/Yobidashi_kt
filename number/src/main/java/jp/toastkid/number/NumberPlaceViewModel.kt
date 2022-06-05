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
import jp.toastkid.number.model.NumberBoard
import jp.toastkid.number.model.NumberPlaceGame
import jp.toastkid.number.repository.GameRepositoryImplementation
import java.io.File

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

    fun loading(): State<Boolean> = _loading

    fun saveCurrentGame(context: Context) {
        val preferenceApplier = PreferenceApplier(context)
        if (preferenceApplier.lastNumberPlaceGamePath().isNullOrBlank()) {
            val dir = File(context.filesDir, "number/place/games")
            if (dir.exists().not()) {
                dir.mkdirs()
            }

            val file = File(dir, "saved_game")

            if (file.exists().not()) {
                file.createNewFile()
            }
            preferenceApplier.setLastNumberPlaceGamePath(file.name)
        }

        val pathname = preferenceApplier.lastNumberPlaceGamePath() ?: return
        GameRepositoryImplementation().save(File(context.filesDir, "number/place/games/$pathname"), _game.value)
    }

}