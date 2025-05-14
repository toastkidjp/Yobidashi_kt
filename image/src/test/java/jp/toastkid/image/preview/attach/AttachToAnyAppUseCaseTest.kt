/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.attach

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.intent.BitmapShareIntentFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class AttachToAnyAppUseCaseTest {

    @InjectMockKs
    private lateinit var attachToAnyAppUseCase: AttachToAnyAppUseCase

    @MockK
    private lateinit var activityStarter: (Intent) -> Unit

    @MockK
    private lateinit var intentFactory: BitmapShareIntentFactory

    @MockK
    private lateinit var intent: Intent

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var bitmap: Bitmap

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { activityStarter.invoke(any()) }.returns(Unit)
        every { intentFactory.invoke(any(), any()) }.returns(intent)

        mockkStatic(Intent::class)
        every { Intent.createChooser(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        attachToAnyAppUseCase(context, bitmap)

        verify(exactly = 1) { activityStarter.invoke(any()) }
        verify(exactly = 1) { intentFactory.invoke(any(), any()) }
        verify(exactly = 1) { Intent.createChooser(any(), any()) }
    }

}
