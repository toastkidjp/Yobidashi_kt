/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.model.tab

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import jp.toastkid.lib.R

/**
 * Start-up view definition.
 *
 * @param titleId
 * @param radioButtonId
 *
 * @author toastkidjp
 */
enum class StartUp(@StringRes val titleId: Int, @IdRes val radioButtonId: Int) {
    SEARCH(R.string.title_search, 0),
    BROWSER(R.string.title_web, 1),
    BOOKMARK(R.string.title_bookmark, 2);

    companion object {

        /**
         * Find value by name.
         *
         * @param name [String]
         */
        fun findByName(name: String?): StartUp = if (name.isNullOrEmpty()) getDefault() else valueOf(name)

        /**
         * Return default value.
         */
        private fun getDefault(): StartUp = SEARCH
    }
}