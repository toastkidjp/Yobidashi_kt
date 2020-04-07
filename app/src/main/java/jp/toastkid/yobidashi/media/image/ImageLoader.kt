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
class ImageLoader(private val contentResolver: ContentResolver) {

    // TODO Extract array
    operator fun invoke(bucket: String): List<Image> {
        val cursor = MediaStore.Images.Media.query(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_TAKEN
                ),
                "bucket_display_name = ?",
                arrayOf(bucket),
                "datetaken DESC"
        )

        val images = mutableListOf<Image>()

        val dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        val displayNameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

        while (cursor?.moveToNext() == true) {
            images.add(
                    Image(
                            cursor.getString(dataIndex),
                            cursor.getString(displayNameIndex),
                            false
                    )
            )
        }
        return images
    }
}