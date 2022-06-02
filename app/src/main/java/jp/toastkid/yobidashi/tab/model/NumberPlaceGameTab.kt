/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.model

import androidx.annotation.Keep
import jp.toastkid.number.model.NumberPlaceGame
import java.util.UUID

class NumberPlaceGameTab : Tab {

    @Keep
    private val numberPlaceGameTab = true

    private var id = UUID.randomUUID().toString()

    private var game: NumberPlaceGame = NumberPlaceGame()

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) {

    }

    override fun getScrolled(): Int = 0

    override fun title(): String {
        return "Number place: ${id}"
    }

    fun game() = game

    fun setGame(game: NumberPlaceGame) {
        this.game = game
    }

}