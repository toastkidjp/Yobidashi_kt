/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.zip

import android.content.Intent
import android.net.Uri
import androidx.core.app.JobIntentService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ZipLoaderServiceTest {

    @Before
    fun setUp() {
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any(), any<Uri>()) }.returns(mockk())

        mockkStatic(JobIntentService::class)
        every { JobIntentService.enqueueWork(any(), any<Class<Any>>(), any(), any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testStart() {
        ZipLoaderService.start(mockk(), mockk())

        verify { JobIntentService.enqueueWork(any(), any<Class<Any>>(), any(), any()) }
    }

    @Test
    fun onHandleWork() {
    }
}