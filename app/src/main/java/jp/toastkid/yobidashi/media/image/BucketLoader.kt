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

    private val names = mutableSetOf<String>()

    operator fun invoke(): List<Image> {
        names.clear()

        val cursor = MediaStore.Images.Media.query(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
        )

        val buckets = mutableListOf<Image>()
        val parentExtractor = ParentExtractor()

        val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val pathIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        while (cursor?.moveToNext() == true) {
            val path = cursor.getString(pathIndex)
            val parentPath = parentExtractor(path)
            if (parentPath == null || names.contains(parentPath)) {
                continue
            }
            names.add(parentPath)
            buckets.add(Image.makeBucket(cursor.getString(columnIndex), path))
        }
        return buckets.distinct()
    }
}