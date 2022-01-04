/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import androidx.activity.result.ActivityResultLauncher
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.media.music.popup.MediaPlayerPopup
import org.junit.After
import org.junit.Before
import org.junit.Test

class MusicPlayerUseCaseTest {

    @InjectMockKs
    private lateinit var musicPlayerUseCase: MusicPlayerUseCase

    @MockK
    private lateinit var mediaPermissionRequestLauncher: ActivityResultLauncher<((Boolean) -> Unit)?>

    @MockK
    private lateinit var mediaPlayerPopup: MediaPlayerPopup

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { mediaPermissionRequestLauncher.launch(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNoOpByPassingNull() {
        musicPlayerUseCase.invoke(null)

        verify(inverse = true) { mediaPermissionRequestLauncher.launch(any()) }
    }

    @Test
    fun test() {
        musicPlayerUseCase.invoke(mockk())

        verify { mediaPermissionRequestLauncher.launch(any()) }
    }
}