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
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import jp.toastkid.article_viewer.article.data.AppDatabase
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
class ZipLoaderService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val dataBase = AppDatabase.find(this)

        val articleRepository = dataBase.articleRepository()

        val file = intent.getParcelableExtra<Uri>("target") ?: return

        val zipLoader = ZipLoader(articleRepository)
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(file) ?: return@withContext
                    zipLoader.invoke(inputStream)
                } catch (e: IOException) {
                    Timber.e(e)
                    zipLoader.dispose()
                }
            }

            // TODO PreferencesWrapper(this).setLastUpdated(file.lastModified())
            /*progress.visibility = View.GONE
            progress_circular.visibility = View.GONE
            all()*/
            val progressIntent = Intent(ACTION_PROGRESS_BROADCAST)
            progressIntent.putExtra("progress", 100)
            sendBroadcast(progressIntent)
            zipLoader.dispose()
        }
    }

    companion object {

        private const val ACTION_PROGRESS_BROADCAST = "jp.toastkid.articles.importing.progress"

        fun makeProgressBroadcastIntentFilter() = IntentFilter(ACTION_PROGRESS_BROADCAST)

        fun start(context: Context, target: Uri) {
            val intent = Intent(context, ZipLoaderService::class.java)
            intent.putExtra("target", target)
            enqueueWork(context, ZipLoaderService::class.java, 20, intent)
        }
    }
}