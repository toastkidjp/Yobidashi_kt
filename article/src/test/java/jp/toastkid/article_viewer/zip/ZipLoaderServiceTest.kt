/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.zip

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.core.app.JobIntentService
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.data.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class ZipLoaderServiceTest {

    private lateinit var zipLoaderService: ZipLoaderService

    @MockK
    private lateinit var zipLoadProgressBroadcastIntentFactory: ZipLoadProgressBroadcastIntentFactory

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @MockK
    private lateinit var intent: Intent

    @MockK
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { intent.getParcelableExtra<Uri>(any()) }.returns(mockk())
        coEvery { zipLoadProgressBroadcastIntentFactory.invoke(any()) }.returns(mockk())

        zipLoaderService =
            spyk(ZipLoaderService(zipLoadProgressBroadcastIntentFactory, mainDispatcher, ioDispatcher))
        coEvery { zipLoaderService.sendBroadcast(any()) }
        coEvery { zipLoaderService.contentResolver }.returns(contentResolver)
        coEvery { contentResolver.openInputStream(any()) }.returns(mockk())

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any(), any<Uri>()) }.returns(mockk())

        mockkStatic(JobIntentService::class)
        every { JobIntentService.enqueueWork(any(), any<Class<Any>>(), any(), any()) }.just(Runs)

        mockkObject(AppDatabase)
        every { AppDatabase.find(any()).articleRepository() }.returns(mockk())

        mockkStatic(Timber::class)
        coEvery { Timber.e(any<Throwable>()) }.just(Runs)

        mockkConstructor(ZipLoader::class)
        coEvery { anyConstructed<ZipLoader>().invoke(any()) }.just(Runs)
        coEvery { anyConstructed<ZipLoader>().dispose() }.just(Runs)
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

}