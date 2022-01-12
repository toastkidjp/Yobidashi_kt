/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.zip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import jp.toastkid.article_viewer.article.data.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

/**
 * @author toastkidjp
 */
@RequiresApi(Build.VERSION_CODES.N)
class ZipLoaderService(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val articleRepository = AppDatabase.find(this).articleRepository()

        val file = intent.getParcelableExtra<Uri>("target") ?: return

        val zipLoader = ZipLoader(articleRepository)
        CoroutineScope(mainDispatcher).launch {
            withContext(ioDispatcher) {
                try {
                    val inputStream = contentResolver.openInputStream(file) ?: return@withContext
                    zipLoader.invoke(inputStream)
                } catch (e: IOException) {
                    Timber.e(e)
                    zipLoader.dispose()
                }
            }

            sendBroadcast(ZipLoadProgressBroadcastIntentFactory()(100))
            zipLoader.dispose()
        }
    }

    companion object {

        fun start(context: Context, target: Uri) {
            val intent = Intent(context, ZipLoaderService::class.java)
            intent.putExtra("target", target)
            enqueueWork(context, ZipLoaderService::class.java, 20, intent)
        }
    }
}