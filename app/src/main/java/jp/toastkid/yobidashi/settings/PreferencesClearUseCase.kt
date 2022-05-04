/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater

/**
 * @author toastkidjp
 */
class PreferencesClearUseCase(
    private val context: Context,
    private val contentViewModel: ContentViewModel?
) {

    operator fun invoke() {
        PreferenceApplier(context).clear()
        Updater().update(context)
        contentViewModel?.snackShort(R.string.done_clear)
    }

    companion object {

        fun make(context: Context): PreferencesClearUseCase {
            return PreferencesClearUseCase(
                context,
                (context as? ViewModelStoreOwner)?.let {
                    ViewModelProvider(it).get(ContentViewModel::class.java)
                }
            )
        }
    }

}