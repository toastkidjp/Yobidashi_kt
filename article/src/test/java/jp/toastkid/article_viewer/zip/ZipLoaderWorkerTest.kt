/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.zip

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import jp.toastkid.article_viewer.article.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import java.io.InputStream
import java.util.concurrent.Executor

class ZipLoaderWorkerTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var executor: Executor

    @MockK
    private lateinit var contentResolver: ContentResolver

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver }.returns(contentResolver)
        every { contentResolver.openInputStream(any()) }.returns(InputStream.nullInputStream())

        mockkObject(AppDatabase)
        every { AppDatabase.find(context).articleRepository() }.returns(mockk())

        mockkConstructor(ZipLoader::class)
        every { anyConstructed<ZipLoader>().invoke(any()) }.just(Runs)

        mockkConstructor(ZipLoadProgressBroadcastIntentFactory::class)
        every { anyConstructed<ZipLoadProgressBroadcastIntentFactory>().invoke(any()) }.returns(
            mockk())

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        mockkObject(ZipLoaderWorker)
        every { ZipLoaderWorker.ioDispatcher() }.returns(Dispatchers.Unconfined)
        every { ZipLoaderWorker.mainDispatcher() }.returns(Dispatchers.Unconfined)
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun doWork() {
        val worker = TestWorkerBuilder<ZipLoaderWorker>(
            context = context,
            executor = executor
        ).build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @org.junit.Test
    fun doWork2() {
        val worker = TestWorkerBuilder<ZipLoaderWorker>(
            context = context,
            executor = executor
        )
            .setInputData(Data.Builder().putString("target", "file://test/file/a.zip").build())
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }
}