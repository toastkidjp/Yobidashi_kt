/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.list

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import jp.toastkid.image.Image
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
internal class ImageLoaderUseCase(
        private val preferenceApplier: PreferenceApplier,
        private val submitImages: (List<Image>) -> Unit,
        private val bucketLoader: BucketLoader,
        private val imageLoader: ImageLoader,
        private val backHandlerState: MutableState<Boolean>,
        private val refreshContent: () -> Unit,
        @VisibleForTesting private val parentExtractor: ParentExtractor = ParentExtractor()
) {

    private var currentBucket: String? = null

    operator fun invoke() {
        invoke(currentBucket)
    }

    operator fun invoke(bucket: String?) {
        val excludedItemFilter = ExcludingItemFilter(preferenceApplier.excludedItems())

        val sort = Sort.findByName(preferenceApplier.imageViewerSort())

        val newList = if (bucket.isNullOrBlank()) {
            bucketLoader(sort)
                    .filter { excludedItemFilter(parentExtractor(it.path)) }
        } else {
            imageLoader(sort, bucket).filter { excludedItemFilter(it.path) }
        }
        submitImages(newList)

        currentBucket = bucket
        backHandlerState.value = currentBucket != null
        refreshContent()
    }

    fun clearCurrentBucket() {
        currentBucket = null
    }

    fun back(onExit: () -> Unit) {
        if (currentBucket == null) {
            onExit()
            return
        }

        clearCurrentBucket()
        invoke()
        backHandlerState.value = currentBucket != null
    }

}