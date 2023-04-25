/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.domain.model

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.unmockkAll
import org.junit.Assert.assertEquals
import org.junit.Test

class TatamiCountConverterTest {

    @InjectMockKs
    private lateinit var tatamiCountConverter: TatamiCountConverter

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        assertEquals("12.96", tatamiCountConverter.firstInputAction("8"))
        assertEquals("18.52", tatamiCountConverter.secondInputAction("30"))
    }

}