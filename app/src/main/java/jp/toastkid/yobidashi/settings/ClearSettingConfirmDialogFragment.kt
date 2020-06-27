/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.libs.HtmlCompat
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.ContentViewModel

/**
 * @author toastkidjp
 */
class ClearSettingConfirmDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val fragmentActivity = activity
                ?: return super.onCreateDialog(savedInstanceState)

        val preferenceApplier = PreferenceApplier(fragmentActivity)

        return AlertDialog.Builder(fragmentActivity)
                .setTitle(R.string.title_clear_settings)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    preferenceApplier.clear()
                    Updater.update(fragmentActivity)
                    ViewModelProvider(fragmentActivity).get(ContentViewModel::class.java)
                            .snackShort(R.string.done_clear)
                    d.dismiss()
                }
                .show()
    }
}