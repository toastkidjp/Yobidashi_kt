/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.zip

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import androidx.core.net.toFile
import androidx.core.net.toUri
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.lib.FileExtractorFromUri
import okio.Okio
import timber.log.Timber
import java.io.File

/**
 * @author toastkidjp
 */
@RequiresApi(Build.VERSION_CODES.N)
class ZipLoaderService : JobIntentService() {

    @SuppressLint("CheckResult")
    override fun onHandleWork(intent: Intent) {
        val dataBase = AppDatabase.find(this)

        val articleRepository = dataBase.diaryRepository()

        val file = intent.getParcelableExtra<Uri>("target") ?: return

        val zipLoader = ZipLoader(articleRepository)
        Completable.fromAction {
            val inputStream = contentResolver.openInputStream(file) ?: return@fromAction
            zipLoader.invoke(inputStream)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // TODO PreferencesWrapper(this).setLastUpdated(file.lastModified())
                    /*progress.visibility = View.GONE
                    progress_circular.visibility = View.GONE
                    all()*/
                    val progressIntent = Intent(ACTION_PROGRESS_BROADCAST)
                    progressIntent.putExtra("progress", 100)
                    sendBroadcast(progressIntent)
                    zipLoader.dispose()
                },
                {
                    Timber.e(it)
                    zipLoader.dispose()
                    /*progress.visibility = View.GONE
                    progress_circular.visibility = View.GONE*/
                }
            )
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