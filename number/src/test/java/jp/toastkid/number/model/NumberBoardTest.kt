/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class NumberBoardTest {

    private lateinit var numberBoard: NumberBoard

    private lateinit var invalidBoard: NumberBoard

    @org.junit.Before
    fun setUp() {
        numberBoard = Json.decodeFromString(
            "{\"rows\":[[2,8,7,3,9,4,5,6,1],[5,6,9,8,1,2,7,3,4],[4,3,1,5,6,7,9,8,2]," +
                    "[9,4,8,2,5,1,6,7,3],[3,2,5,6,7,8,4,1,9],[7,1,6,4,3,9,2,5,8]," +
                    "[1,9,3,7,2,6,8,4,5],[6,5,4,9,8,3,1,2,7],[8,7,2,1,4,5,3,9,6]]}"
        )

        invalidBoard = Json.decodeFromString(
            "{\"rows\":[[2,8,7,3,9,4,5,6,1],[5,6,9,8,1,2,7,3,4],[4,3,1,5,6,7,9,8,2]," +
                    "[9,4,-1,2,5,1,6,7,3],[3,2,5,6,7,8,4,1,9],[7,1,6,4,3,9,2,5,8]," +
                    "[1,9,3,7,2,6,8,4,5],[6,5,4,9,8,3,1,2,7],[8,7,2,1,4,5,3,9,6]]}"
        )
    }

    @org.junit.Test
    fun fillZero() {
        val numberBoard1 = NumberBoard()
        numberBoard1.fillZero()

        assertTrue(numberBoard1.fulfilled())
    }

    @org.junit.Test
    fun placeRandom() {
        numberBoard.placeRandom()
    }

    @org.junit.Test
    fun masked() {
        val masked = numberBoard.masked(20)

        assertFalse(masked.fulfilled())
    }

    @org.junit.Test
    fun copyFrom() {
        invalidBoard.copyFrom(numberBoard)

        assertTrue(numberBoard.isCorrect(invalidBoard))
    }

    @org.junit.Test
    fun rows() {
        assertEquals(9, numberBoard.rows().size)
    }

    @org.junit.Test
    fun isCorrect() {
        assertTrue(numberBoard.isCorrect(numberBoard))
    }

    @org.junit.Test
    fun isIncorrect() {
        assertFalse(invalidBoard.isCorrect(numberBoard))
    }

    @org.junit.Test
    fun place() {
        invalidBoard.place(0, 0, 3)

        assertEquals(3, invalidBoard.pick(0, 0))
    }

    @org.junit.Test
    fun fulfilled() {
        assertFalse(invalidBoard.fulfilled())
    }

}