/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.user_agent

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class UserAgentDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickUserAgent(userAgent: UserAgent)
    }

    private var onClick: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment
        if (targetFragment is Callback) {
            onClick = targetFragment
        }

        val preferenceApplier = PreferenceApplier(activityContext)

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_user_agent)
                .setSingleChoiceItems(
                        UserAgent.titles(),
                        UserAgent.findCurrentIndex(preferenceApplier.userAgent())
                ) { d, i ->
                    val userAgent = UserAgent.values()[i]
                    preferenceApplier.setUserAgent(userAgent.name)
                    onClick?.onClickUserAgent(userAgent)
                    d.dismiss()
                }
                .setNegativeButton(R.string.close) { d, _ -> d.cancel() }
                .create()
    }

}