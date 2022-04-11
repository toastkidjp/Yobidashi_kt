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
import androidx.core.content.ContextCompat
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconFolderProviderService
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.browser.icon.WebClipIconLoader
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.color.DefaultColorInsertion

/**
 * @param preferenceApplier
 */
class FirstLaunchInitializer(
    private val context: Context,
    private val preferenceApplier: PreferenceApplier,
    @VisibleForTesting private val defaultColorInsertion: DefaultColorInsertion = DefaultColorInsertion(),
    @VisibleForTesting private val faviconFolderProviderService: FaviconFolderProviderService = FaviconFolderProviderService(),
    @VisibleForTesting private val defaultBackgroundImagePreparation: DefaultBackgroundImagePreparation = DefaultBackgroundImagePreparation()
) {

    /**
     * Process for first launch.
     */
    operator fun invoke() {
        if (!preferenceApplier.isFirstLaunch()) {
            return
        }

        preferenceApplier.color = ContextCompat.getColor(context, R.color.colorPrimaryDark)

        defaultColorInsertion.insert(context)
        BookmarkInitializer(faviconFolderProviderService.invoke(context), WebClipIconLoader.from(context))(context)
        defaultBackgroundImagePreparation(context) {
            preferenceApplier.backgroundImagePath = it.absolutePath
        }

        preferenceApplier.setDefaultSearchEngine(SearchCategory.getDefaultCategoryName())
    }

}