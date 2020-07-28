/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.cleaner

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ProcessCleanerInvoker {
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    operator fun invoke(snackbarParent: View) {
        val context = snackbarParent.context
        val preferenceApplier = PreferenceApplier(context)

        if (UsageStatsPermissionChecker()
                        .invoke(
                                context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager,
                                context.packageName)
        ) {
            snackConfirmRequirePermission(snackbarParent, preferenceApplier)
            return
        }

        var snackbar: Snackbar? = null

        CoroutineScope(Dispatchers.Main).launch {
            val cleaner = withContext(Dispatchers.Default) {
                snackbar = Toaster.snackIndefinite(
                        snackbarParent,
                        "Start cleaning...",
                        preferenceApplier.colorPair()
                )
                ProcessCleaner().invoke(
                        context.packageManager,
                        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager,
                        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                )
            }
            snackbar?.dismiss()
            onSuccess(snackbarParent, cleaner, preferenceApplier)
        }
    }

    private fun onSuccess(
            snackbarParent: View,
            message: String,
            preferenceApplier: PreferenceApplier
    ) {
        Toaster.snack(
                snackbarParent,
                if (message.isBlank()) "Failed." else message,
                preferenceApplier.colorPair(),
                Snackbar.LENGTH_LONG
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun snackConfirmRequirePermission(
            snackbarParent: View,
            preferenceApplier: PreferenceApplier
    ) {
        Toaster.withAction(
                snackbarParent,
                R.string.message_require_usage_stats_permission,
                R.string.action_settings,
                View.OnClickListener {
                    snackbarParent.context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
                preferenceApplier.colorPair(),
                Snackbar.LENGTH_INDEFINITE
        )
    }
}