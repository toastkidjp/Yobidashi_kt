/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.initial

import android.os.Bundle
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.search.SearchFragment

/**
 * @author toastkidjp
 */
class InitialIndexSettingUseCase {

    fun put(arguments: Bundle?, javaClass: Class<Fragment>?) {
        arguments?.putInt(
                KEY_EXTRA_INITIAL_INDEX,
                when (javaClass) {
                    SearchFragment::class.java -> 2
                    EditorFragment::class.java -> 4
                    else -> 0
                }
        )
    }

    fun extract(arguments: Bundle?): Int = arguments?.getInt(KEY_EXTRA_INITIAL_INDEX) ?: 0

    companion object {

        private const val KEY_EXTRA_INITIAL_INDEX = "initialIndex"

    }
}