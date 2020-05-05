/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.permission

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class RuntimePermissionProcessorFragment : Fragment() {

    private val channels: MutableMap<String, Channel<RequestPermissionResult>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun request(permission: String): Channel<RequestPermissionResult> {
        val channel = Channel<RequestPermissionResult>()
        channels.put(permission, channel)
        requestPermissions(arrayOf(permission), PERMISSIONS_REQUEST_CODE)
        return channel
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            return
        }

        onRequestPermissionsResult(permissions, grantResults)
    }

    private fun onRequestPermissionsResult(
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        permissions.forEachIndexed { index, permission ->
            val channel = channels[permission] ?: Channel()
            channels.remove(permission)
            val granted = grantResults[index] == PackageManager.PERMISSION_GRANTED
            CoroutineScope(Dispatchers.Default).launch {
                channel.send(RequestPermissionResult(permission, granted))
            }
        }
    }

    companion object {

        private const val PERMISSIONS_REQUEST_CODE = 42

        private val TAG = RuntimePermissionProcessorFragment::class.java.canonicalName

        fun obtainFragment(activity: FragmentActivity): RuntimePermissionProcessorFragment {
            val fragment: RuntimePermissionProcessorFragment? = findFragment(activity)
            if (fragment != null) {
                return fragment
            }

            val newFragment = RuntimePermissionProcessorFragment()
            val fragmentManager = activity.supportFragmentManager
            fragmentManager
                    .beginTransaction()
                    .add(newFragment, TAG)
                    .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
            return newFragment
        }

        private fun findFragment(activity: FragmentActivity): RuntimePermissionProcessorFragment? {
            return activity.supportFragmentManager.findFragmentByTag(TAG)
                    as? RuntimePermissionProcessorFragment
        }
    }
}