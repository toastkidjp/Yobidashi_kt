/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.initial

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.color.DefaultColorInsertion

/**
 * @param preferenceApplier
 */
class FirstLaunchInitializer(
    private val context: Context,
    private val preferenceApplier: PreferenceApplier,
    @VisibleForTesting private val defaultColorInsertion: DefaultColorInsertion = DefaultColorInsertion(),
    @VisibleForTesting private val defaultBackgroundImagePreparation: DefaultBackgroundImagePreparation = DefaultBackgroundImagePreparation()
) {

    /**
     * Process for first launch.
     */
    operator fun invoke() {
        if (!preferenceApplier.isFirstLaunch()) {
            return
        }

        preferenceApplier.color = Color(0xFF000044).toArgb()

        defaultColorInsertion.insert(context)
        BookmarkInitializer.from(context)()
        defaultBackgroundImagePreparation(context) {
            preferenceApplier.backgroundImagePath = it.absolutePath
        }

        preferenceApplier.setDefaultSearchEngine(SearchCategory.getDefaultCategoryName())
    }

}