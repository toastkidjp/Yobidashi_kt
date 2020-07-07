/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.cleaner

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/**
 * @author toastkidjp
 */
class TargetProcessFilter(
        private val packageManager: PackageManager,
        private val notIgnoreProcessFilter: NotIgnoreProcessFilter
) {

    operator fun invoke(processName: String): Boolean {
        val installedApplications =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                        .filter { notIgnoreProcessFilter(it) }
                        .map { it.packageName }

        val homeApp = packageManager
                .queryIntentActivities(intent, flag)
                .map { it.activityInfo.packageName }

        return !homeApp.contains(processName) && installedApplications.contains(processName)
    }

    companion object {

        private val intent =
                Intent(Intent.ACTION_MAIN).also { it.addCategory(Intent.CATEGORY_HOME) }

        private val flag =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL
                else 0

    }

}