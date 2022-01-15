/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.list

import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
internal class ImageFilterUseCase(
        private val preferenceApplier: PreferenceApplier,
        private val adapter: Adapter?,
        private val imageLoaderUseCase: ImageLoaderUseCase,
        private val imageLoader: ImageLoader,
        private val refreshContent: () -> Unit
) {

    operator fun invoke(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            imageLoaderUseCase()
            return
        }

        val excludedItemFilter = ExcludingItemFilter(preferenceApplier.excludedItems())

        val newList = imageLoader.filterBy(keyword).filter { excludedItemFilter(it.path) }
        adapter?.submitList(newList)

        imageLoaderUseCase.clearCurrentBucket()

        refreshContent()
    }

}