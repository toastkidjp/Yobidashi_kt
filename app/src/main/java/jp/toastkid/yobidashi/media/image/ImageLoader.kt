/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore

/**
 * @author toastkidjp
 */
class ImageLoader(private val contentResolver: ContentResolver) {

    operator fun invoke(sort: Sort, bucket: String): List<Image> {
        return extractImages(
                MediaStore.Images.Media.query(
                        contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        columns,
                        "bucket_display_name = ?",
                        arrayOf(bucket),
                        sort.imageSort
                )
        )
    }

    fun filterBy(name: String?): List<Image> {
        return extractImages(
                MediaStore.Images.Media.query(
                        contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        columns,
                        "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?",
                        arrayOf("%$name%"),
                        Sort.NAME.imageSort
                )
        )
    }

    private fun extractImages(cursor: Cursor?): MutableList<Image> {
        val images = mutableListOf<Image>()

        val dataIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DATA) ?: 0
        val displayNameIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME) ?: 0

        while (cursor?.moveToNext() == true) {
            images.add(Image(cursor.getString(dataIndex), cursor.getString(displayNameIndex)))
        }
        return images
    }

    companion object {
        private val columns = arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
        )
    }
}