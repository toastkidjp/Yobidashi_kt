/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.content.ContentResolver
import android.provider.MediaStore

/**
 * @author toastkidjp
 */
class BucketLoader(private val contentResolver: ContentResolver) {

    operator fun invoke(): List<Image> {
        val cursor = MediaStore.Images.Media.query(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        )

        val buckets = mutableListOf<Image>()

        val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (cursor?.moveToNext() == true) {
            buckets.add(Image.makeBucket(cursor.getString(columnIndex)))
        }
        return buckets.distinct()
    }
}