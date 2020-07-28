/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.cleaner

import android.content.pm.ApplicationInfo

/**
 * @author toastkidjp
 */
class NotIgnoreProcessFilter {

    operator fun invoke(applicationInfo: ApplicationInfo): Boolean {
        if (applicationInfo.flags and PRIMARY_FLAG == PRIMARY_FLAG) {
            return false
        }
        return !ignoreProcesses.contains(applicationInfo.packageName)
    }

    companion object {

        private const val PRIMARY_FLAG =
                ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_PERSISTENT

        private val ignoreProcesses = setOf(
                "system",
                "com.android.phone",
                "android.process.acore",
                "android.process.media",
                "com.android.inputmethod",
                "com.android.bluetooth",
                "com.android.systemui",
                "com.android.smspush",
                "com.android.chrome",
                "jp.toastkid.yobidashi.d",
                "jp.toastkid.yobidashi"
        )

    }
}