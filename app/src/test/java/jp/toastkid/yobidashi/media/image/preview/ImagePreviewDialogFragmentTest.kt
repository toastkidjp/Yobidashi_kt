/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview

import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImagePreviewDialogFragmentTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putSerializable(any(), any()) }.answers { Unit }
        every { anyConstructed<Bundle>().putInt(any(), any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testWithImage() {
        ImagePreviewDialogFragment.withImages(listOf(mockk()), 1)

        verify(exactly = 1) { anyConstructed<Bundle>().putSerializable(any(), any()) }
        verify(exactly = 1) { anyConstructed<Bundle>().putInt(any(), any()) }
    }

}