/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.repository

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.number.model.NumberBoard
import jp.toastkid.number.model.NumberPlaceGame
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.PrintWriter

class GameRepositoryImplementationTest {

    @InjectMockKs
    private lateinit var gameRepository: GameRepositoryImplementation

    @MockK
    private lateinit var file: File

    private lateinit var game: NumberPlaceGame

    @MockK
    private lateinit var writer: PrintWriter

    @MockK
    private lateinit var reader: BufferedReader

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        game = NumberPlaceGame()

        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")
        every { file.writeText(any()) }.just(Runs)
        every { file.readText() }.returns("test")
        every { file.delete() }.returns(true)
        every { writer.println(any<String>()) }.just(Runs)
        every { writer.flush() }.just(Runs)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun save() {
        gameRepository.save(file, game)

        verify { file.writeText(any()) }
    }

    @org.junit.Test
    fun load() {
        val board = Json.decodeFromString<NumberBoard>("{\"rows\":[[2,8,7,3,9,4,5,6,1],[5,6,9,8,1,2,7,3,4],[4,3,1,5,6,7,9,8,2],[9,4,8,2,5,1,6,7,3],[3,2,5,6,7,8,4,1,9],[7,1,6,4,3,9,2,5,8],[1,9,3,7,2,6,8,4,5],[6,5,4,9,8,3,1,2,7],[8,7,2,1,4,5,3,9,6]]}")
        println(board)

        val game = Json.decodeFromString<NumberPlaceGame>(
            "{\"correct\":{\"rows\":[[2,8,7,3,9,4,5,6,1],[5,6,9,8,1,2,7,3,4],[4,3,1,5,6,7,9,8,2],[9,4,8,2,5,1,6,7,3],[3,2,5,6,7,8,4,1,9],[7,1,6,4,3,9,2,5,8],[1,9,3,7,2,6,8,4,5],[6,5,4,9,8,3,1,2,7],[8,7,2,1,4,5,3,9,6]]}," +
                    "\"masked\":{\"rows\":[[2,-1,-1,3,9,4,-1,-1,1],[-1,-1,-1,8,1,-1,-1,-1,-1],[-1,3,-1,-1,-1,7,9,8,2],[-1,4,8,2,5,-1,-1,7,-1],[3,2,-1,-1,-1,-1,-1,-1,-1],[7,1,6,4,3,-1,-1,-1,-1],[-1,9,-1,7,2,-1,-1,4,-1],[6,-1,4,9,8,-1,1,-1,-1],[-1,-1,2,-1,4,-1,-1,9,6]]},\"solving\":{\"rows\":[[2,-1,-1,3,9,4,-1,-1,1],[-1,-1,-1,8,1,-1,-1,-1,-1],[-1,3,-1,-1,-1,7,9,8,2],[-1,4,8,2,5,-1,-1,7,-1],[3,2,-1,-1,-1,4,5,-1,6],[7,1,6,4,3,-1,-1,-1,-1],[-1,9,-1,7,2,-1,-1,4,-1],[6,-1,4,9,8,-1,1,-1,-1],[-1,-1,2,-1,4,-1,-1,9,6]]}}"
        )
        println(game)
    }

    @org.junit.Test
    fun delete() {
        gameRepository.delete(file)

        every { file.delete() }
    }

}