/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.widget

import android.graphics.Color
import android.widget.RemoteViews
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.VectorToBitmap
import org.junit.After
import org.junit.Before
import org.junit.Test

class IconInitializerTest {

    @InjectMockKs
    private lateinit var iconInitializer: IconInitializer

    @MockK
    private lateinit var vectorToBitmap: VectorToBitmap

    @MockK
    private lateinit var remoteViews: RemoteViews

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { vectorToBitmap.invoke(any()) }.returns(mockk())
        every { remoteViews.setImageViewBitmap(any(), any()) }.answers { Unit }
        every { remoteViews.setInt(any(), any(), any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        iconInitializer.invoke(remoteViews, Color.WHITE, 0, 0)

        verify(exactly = 1) { vectorToBitmap.invoke(any()) }
        verify(exactly = 1) { remoteViews.setImageViewBitmap(any(), any()) }
        verify(exactly = 1) { remoteViews.setInt(any(), any(), any()) }
    }

}