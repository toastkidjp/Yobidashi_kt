/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class SpeechMakerTest {

    @InjectMockKs
    private lateinit var speechMaker: SpeechMaker

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var textToSpeech: TextToSpeech

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { textToSpeech.stop() }.returns(0)
        every { textToSpeech.isSpeaking }.returns(false)
        every { textToSpeech.speak(any(), any(), any(), any()) }.returns(1)
        every { textToSpeech.shutdown() }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSpeakCase() {
        speechMaker.invoke("test")

        verify(exactly = 1) { textToSpeech.speak(any(), any(), any(), any()) }
        verify(exactly = 0) { textToSpeech.stop() }
    }

}