/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.favorite

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class ClearFavoriteSearchDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_delete_all)
                .setMessage(
                    Html.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    )
                )
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    d.dismiss()
                }
                .create()
    }

    companion object {

        private const val KEY_VIEW_MODEL = "view_model"

        fun show(fragmentManager: FragmentManager, viewModel: FavoriteSearchFragmentViewModel) {
            val dialogFragment = ClearFavoriteSearchDialogFragment()
            dialogFragment.arguments = bundleOf(KEY_VIEW_MODEL to viewModel)
            dialogFragment.show(
                    fragmentManager,
                    ClearFavoriteSearchDialogFragment::class.java.canonicalName
            )
        }
    }
}
