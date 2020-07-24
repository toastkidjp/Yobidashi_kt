/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.list

import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
internal class ImageLoaderUseCase(
        private val preferenceApplier: PreferenceApplier,
        private val adapter: Adapter?,
        private val bucketLoader: BucketLoader,
        private val imageLoader: ImageLoader,
        private val refreshContent: () -> Unit
) {

    private val parentExtractor = ParentExtractor()

    private var currentBucket: String? = null

    operator fun invoke() {
        invoke(currentBucket)
    }

    operator fun invoke(bucket: String?) {
        adapter?.clear()

        val excludedItemFilter = ExcludingItemFilter(preferenceApplier.excludedItems())

        val sort = Sort.findByName(preferenceApplier.imageViewerSort())

        if (bucket.isNullOrBlank()) {
            bucketLoader(sort)
                    .filter { excludedItemFilter(parentExtractor(it.path)) }
        } else {
            imageLoader(sort, bucket).filter { excludedItemFilter(it.path) }
        }
                .forEach { adapter?.add(it) }
        currentBucket = bucket
        refreshContent()
    }

    fun clearCurrentBucket() {
        currentBucket = null
    }

}