/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.permission

import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel

/**
 * @author toastkidjp
 */
class RuntimePermissions(private val activity: FragmentActivity) {

    private val fragment = RuntimePermissionProcessorFragment.obtainFragment(activity)

    fun request(permission: String): Channel<RequestPermissionResult>? {
        return fragment.request(permission)
    }

    fun isGranted(permission: String): Boolean {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isRevoked(permission: String): Boolean {
        return !isGranted(permission)
    }
}