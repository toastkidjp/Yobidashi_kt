/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.cleaner

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

/**
 * @author toastkidjp
 */
class ProcessCleaner {

    operator fun invoke(
            packageManager: PackageManager,
            activityManager: ActivityManager?,
            usageStatsManager: UsageStatsManager?
    ): String {
        if (activityManager == null || usageStatsManager == null) {
            return ""
        }

        val preInformation = getMemoryInformation(activityManager)
        val preAvailableMemories = (preInformation.availMem / 1024 / 1024).toInt()

        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { isTarget(it) }
                .map { it.packageName }

        val homeApp = packageManager
                .queryIntentActivities(intent, flag)
                .map { it.activityInfo.packageName }

        val runningAppProcesses = getRunningProcesses(usageStatsManager)
        Timber.i("runningAppProcesses")
        runningAppProcesses.forEach { Timber.i("Process ${it}") }

        if (runningAppProcesses.isEmpty()) {
            return ""
        }

        val killingTargetProcesses = runningAppProcesses
                .filter { !homeApp.contains(it) && installedApplications.contains(it) }
        killingTargetProcesses
                .forEach {
                    activityManager.killBackgroundProcesses(it)
                    Timber.i("Process Killed $it")
                }

        val postInformation = getMemoryInformation(activityManager)
        val availableMemories = (postInformation.availMem / 1024 / 1024).toInt()
        val lowMemory = postInformation.lowMemory

        return "${preAvailableMemories - availableMemories} [MB], low memory: $lowMemory"
    }

    @SuppressLint("NewApi")
    private fun getRunningProcesses(usageStatsManager: UsageStatsManager): List<String> {
        val currentTimeMillis = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(currentTimeMillis - 600_000L, currentTimeMillis);

        val processes = mutableListOf<String>()

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            processes.add(event.packageName)
        }

        return processes
    }

    private fun getMemoryInformation(activityManager: ActivityManager): ActivityManager.MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    private fun isTarget(applicationInfo: ApplicationInfo): Boolean {
        if (applicationInfo.flags and primaryFlag == primaryFlag) {
            return false
        }
        return !ignoreProcesses.contains(applicationInfo.packageName)

    }

    companion object {

        private val intent = Intent(Intent.ACTION_MAIN).also { it.addCategory(Intent.CATEGORY_HOME) }

        private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else 0

        private val primaryFlag = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_PERSISTENT

        private val ignoreProcesses = setOf(
                "system",
                "com.android.phone",
                "android.process.acore",
                "android.process.media",
                "com.android.inputmethod",
                "com.android.bluetooth",
                "com.android.smspush",
                "com.android.chrome",
                "jp.toastkid.yobidashi.d",
                "jp.toastkid.yobidashi"
        )
    }
}