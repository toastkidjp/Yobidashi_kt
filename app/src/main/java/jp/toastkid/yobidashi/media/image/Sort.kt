/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.provider.MediaStore

/**
 * @author toastkidjp
 */
enum class Sort(val bucketSort: String, val imageSort: String) {
    DATE(
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
    ),
    NAME(
            "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} ASC",
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
    ),
    ITEM_COUNT(
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
    );

    companion object {

        fun default() = DATE

        fun findByName(name: String?) =
                if (name.isNullOrBlank()) default()
                else values().firstOrNull { it.name == name } ?: default()
    }
}