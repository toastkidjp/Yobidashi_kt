/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.zip

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import jp.toastkid.article_viewer.article.data.ArticleRepositoryFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

class ZipLoaderWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val zipLoadProgressBroadcastIntentFactory: ZipLoadProgressBroadcastIntentFactory =
        ZipLoadProgressBroadcastIntentFactory()

    override fun doWork(): Result {
        val file = workerParams.inputData.getString("target")?.toUri() ?: return Result.failure()

        val articleRepository = ArticleRepositoryFactory().invoke(context)
        val zipLoader = ZipLoader(articleRepository)

        CoroutineScope(mainDispatcher()).launch {
            withContext(ioDispatcher()) {
                try {
                    val inputStream = context.contentResolver.openInputStream(file) ?: return@withContext
                    zipLoader.invoke(inputStream)
                } catch (e: IOException) {
                    Timber.e(e)
                    zipLoader.dispose()
                }
            }

            context.sendBroadcast(zipLoadProgressBroadcastIntentFactory(100))
            zipLoader.dispose()
        }

        return Result.success()
    }

    companion object {

        fun mainDispatcher(): CoroutineDispatcher = Dispatchers.Main

        fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

        fun start(context: Context, target: Uri) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "import_articles",
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequest
                    .Builder(ZipLoaderWorker::class.java)
                    .setInputData(Data.Builder().putString("target", target.toString()).build())
                    .build()
            )
        }

    }

}