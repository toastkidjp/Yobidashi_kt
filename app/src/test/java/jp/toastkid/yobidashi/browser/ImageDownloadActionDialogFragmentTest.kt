/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageDownloadActionDialogFragmentTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putCharSequence(any(), any()) }.answers { Unit }

        mockkConstructor(ImageDownloadActionDialogFragment::class)
        every { anyConstructed<ImageDownloadActionDialogFragment>().setArguments(any()) }.answers { Unit }
        every { anyConstructed<ImageDownloadActionDialogFragment>().show(any<FragmentManager>(), any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIncorrectTypeArgumentCase() {
        val context = mockk<Context>()

        ImageDownloadActionDialogFragment.show(context, "test")

        verify(exactly = 0) { anyConstructed<Bundle>().putCharSequence(any(), any()) }
        verify(exactly = 0) { anyConstructed<ImageDownloadActionDialogFragment>().setArguments(any()) }
        verify(exactly = 0) { anyConstructed<ImageDownloadActionDialogFragment>().show(any<FragmentManager>(), any()) }
    }

    @Test
    fun testCorrectParameterCase() {
        val context = mockk<FragmentActivity>()
        every { context.getSupportFragmentManager() }.returns(mockk())

        ImageDownloadActionDialogFragment.show(context, "test")

        verify(exactly = 1) { anyConstructed<Bundle>().putCharSequence(any(), any()) }
        verify(exactly = 1) { anyConstructed<ImageDownloadActionDialogFragment>().setArguments(any()) }
        verify(exactly = 1) { anyConstructed<ImageDownloadActionDialogFragment>().show(any<FragmentManager>(), any()) }
        verify(exactly = 1) { context.getSupportFragmentManager() }
    }

}