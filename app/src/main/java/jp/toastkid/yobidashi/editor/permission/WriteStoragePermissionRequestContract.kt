/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class WriteStoragePermissionRequestContract
    : ActivityResultContract<String?, Pair<Boolean, String?>>() {

    private var filePath: String? = null

    override fun createIntent(context: Context, input: String?): Intent {
        filePath = input

        return Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS)
            .putExtra(
                ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, String?> {
        if (resultCode != AppCompatActivity.RESULT_OK) return false to filePath
        val granted = intent
            ?.getIntArrayExtra(ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS)
            ?.getOrNull(0) == PackageManager.PERMISSION_GRANTED
        return granted to filePath
    }

}