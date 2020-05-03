/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.notification.morning

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author toastkidjp
 */
class DailyNotificationWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        enqueueNextWork(context)

        CoroutineScope(Dispatchers.IO).launch { DailyNotification().show(context) }
        return Result.success()
    }

    companion object {

        private const val TAG = "daily_notification"

        private fun calcNextTime(): Long {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            dueDate.set(Calendar.HOUR_OF_DAY, 8)
            dueDate.set(Calendar.MINUTE, 0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            return dueDate.timeInMillis.minus(currentDate.timeInMillis)
        }

        fun enqueueNextWork(context: Context) {
            val dailyWorkRequest = OneTimeWorkRequest.Builder(DailyNotificationWorker::class.java)
                    .setInitialDelay(calcNextTime(), TimeUnit.MILLISECONDS)
                    .addTag(TAG)
                    .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(dailyWorkRequest)
        }

        fun cancelAllWork(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(TAG)
        }
    }
}