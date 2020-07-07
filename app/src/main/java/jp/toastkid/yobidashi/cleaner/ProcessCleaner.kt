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
import android.content.pm.PackageManager

/**
 * @author toastkidjp
 */
class ProcessCleaner {

    private val notIgnoreProcessFilter = NotIgnoreProcessFilter()

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

        val runningAppProcesses = getRunningProcesses(usageStatsManager)
        if (runningAppProcesses.isEmpty()) {
            return ""
        }

        val targetProcessFilter = TargetProcessFilter(packageManager, notIgnoreProcessFilter)
        val killingTargetProcesses = runningAppProcesses.filter { targetProcessFilter(it) }

        if (killingTargetProcesses.isEmpty()) {
            return "Target processes are not found."
        }

        killingTargetProcesses.forEach { activityManager.killBackgroundProcesses(it) }

        val targets = killingTargetProcesses.reduce { base, item -> "$base$lineSeparator$item" }

        val postInformation = getMemoryInformation(activityManager)
        val availableMemories = (postInformation.availMem / 1024 / 1024).toInt()

        return "Get ${availableMemories - preAvailableMemories} [MB],$lineSeparator$targets$lineSeparator"
    }

    @SuppressLint("NewApi")
    private fun getRunningProcesses(usageStatsManager: UsageStatsManager): Set<String> {
        val currentTimeMillis = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(currentTimeMillis - 600_000L, currentTimeMillis)

        val processes = mutableSetOf<String>()

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

    companion object {

        private val lineSeparator = System.getProperty("line.separator")
    }
}