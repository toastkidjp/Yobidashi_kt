/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.translate

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.Assert.assertTrue

class TranslationUrlGeneratorTest {

    @InjectMockKs
    private lateinit var translationUrlGenerator: TranslationUrlGenerator

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Uri::class)
        every { Uri.encode(any()) }.returns("Mocked")
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun makeTranslateUrl() {
        assertTrue(translationUrlGenerator("").contains("sl=en&tl=ja"))
        assertTrue(translationUrlGenerator("Make sense?").contains("sl=en&tl=ja"))
        assertTrue(translationUrlGenerator("きょうは Chicken Noodle だ。").contains("sl=ja&tl=en"))
    }

}