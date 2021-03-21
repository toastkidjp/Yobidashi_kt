/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MonthFormatterUseCaseTest {

    private lateinit var monthFormatterUseCase: MonthFormatterUseCase

    @Before
    fun setUp() {
        monthFormatterUseCase = MonthFormatterUseCase()
    }

    @Test
    fun test() {
        assertEquals("01", monthFormatterUseCase.invoke(0))
        assertEquals("02", monthFormatterUseCase.invoke(1))
        assertEquals("09", monthFormatterUseCase.invoke(8))
        assertEquals("10", monthFormatterUseCase.invoke(9))
        assertEquals("11", monthFormatterUseCase.invoke(10))
        assertEquals("12", monthFormatterUseCase.invoke(11))
    }

}