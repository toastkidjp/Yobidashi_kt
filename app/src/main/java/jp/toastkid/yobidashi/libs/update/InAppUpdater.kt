/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.update

import android.app.Activity
import android.view.View
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class InAppUpdater(private val parent: View) {

    private val manager = AppUpdateManagerFactory.create(parent.context)

    private var listener: InstallStateUpdatedListener? = null

    operator fun invoke() {
        val listener = InstallStateUpdatedListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                showDownloaded()
                manager.unregisterListener(listener)
            }
        }

        manager.registerListener(listener)

        manager.appUpdateInfo.addOnCompleteListener { task ->
            val info = task.result

            val activity = parent.context as? Activity ?: return@addOnCompleteListener

            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, activity, REQUEST_CODE)
            }
        }

    }

    fun onResume() {
        manager.appUpdateInfo.addOnCompleteListener { task ->
            val appUpdateInfo = task.result

            // アップデートファイルがダウンロード済であればSnackbarを表示する
            if(appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                showDownloaded()
            }
        }
    }

    private fun showDownloaded() {
        Toaster.snackIndefinite(
                parent,
                "An update has just been downloaded.",
                PreferenceApplier(parent.context).colorPair()
        )
                .setAction("UPDATE") { manager.completeUpdate() }
                .show()
    }

    companion object {
        private const val REQUEST_CODE = 211
    }
}