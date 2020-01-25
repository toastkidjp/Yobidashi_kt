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
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * @author toastkidjp
 */
class ProcessCleanerInvoker {
    
    operator fun invoke(snackbarParent: View): Disposable {
        val context = snackbarParent.context
        val preferenceApplier = PreferenceApplier(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toaster.snackShort(
                    snackbarParent,
                    R.string.message_cannot_use_under_l,
                    preferenceApplier.colorPair()
            )
            return Disposables.disposed()
        }

        if (UsageStatsPermissionChecker()
                        .invoke(
                                context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager,
                                context.packageName)
        ) {
            snackConfirmRequirePermission(snackbarParent, preferenceApplier)
            return Disposables.disposed()
        }

        var snackbar: Snackbar? = null

        return Single.fromCallable {
            snackbar = Toaster.snackIndefinite(
                    snackbarParent,
                    "Start cleaning...",
                    preferenceApplier.colorPair()
            )
            snackbar?.view
            ProcessCleaner().invoke(
                    context.packageManager,
                    context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager,
                    context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            )
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            snackbar?.dismiss()
                            Toaster.snack(
                                    snackbarParent,
                                    if (it.isBlank()) "Failed." else it,
                                    preferenceApplier.colorPair(),
                                    Snackbar.LENGTH_LONG
                            )
                        },
                        Timber::e
                )
    }

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