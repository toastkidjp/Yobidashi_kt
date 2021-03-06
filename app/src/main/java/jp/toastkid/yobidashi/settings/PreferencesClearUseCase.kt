/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater

/**
 * @author toastkidjp
 */
class PreferencesClearUseCase(
    private val fragmentActivity: FragmentActivity,
    private val viewModelProvider: ViewModelProvider = ViewModelProvider(fragmentActivity)
) {

    operator fun invoke() {
        PreferenceApplier(fragmentActivity).clear()
        Updater().update(fragmentActivity)
        viewModelProvider.get(ContentViewModel::class.java)
            .snackShort(R.string.done_clear)
    }

}