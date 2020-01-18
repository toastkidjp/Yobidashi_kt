/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.cleaner

import android.app.AppOpsManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * @author toastkidjp
 */
class UsageStatsPermissionChecker {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    operator fun invoke(appOps: AppOpsManager?, packageName: String): Boolean {
        val uid = android.os.Process.myUid()
        val mode = appOps?.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, packageName)
        return mode != AppOpsManager.MODE_ALLOWED
    }
}