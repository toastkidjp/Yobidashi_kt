/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.voice

import android.content.Context
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class VoiceSearchTest {

    private lateinit var voiceSearch: VoiceSearch

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        voiceSearch = VoiceSearch()

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any(), any<String>()) }.returns(mockk())

        every { context.getPackageName() }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun makeIntent() {
        voiceSearch.makeIntent()

        verify(atLeast = 1) { anyConstructed<Intent>().putExtra(any(), any<String>()) }
        verify(atLeast = 1) { context.getPackageName() }
    }

    //TODO fix it
    @Test
    fun testSuggestInstallGoogleApp() {
        voiceSearch.suggestInstallGoogleApp(mockk(), mockk())
    }

}