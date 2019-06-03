/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class RecommendColorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_add_recommended_colors)
                .setMessage(R.string.message_add_recommended_colors)
                .setPositiveButton(R.string.ok) { d, _ ->
                    SavedColors.insertDefaultColors(activityContext)
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

}