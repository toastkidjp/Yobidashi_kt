/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview.detail

import androidx.exifinterface.media.ExifInterface

class ExifInformationExtractorUseCase(
        private val stringBuilder: StringBuilder = StringBuilder()
) {
    
    operator fun invoke(exifInterface: ExifInterface): String? {
        TARGET_TAGS.forEach { extractAttribute(exifInterface, it) }

        val content = stringBuilder.toString()
        stringBuilder.setLength(0)
        return content
    }

    private fun extractAttribute(exifInterface: ExifInterface, tag: String) {
        exifInterface.getAttribute(tag)?.let {
            stringBuilder.append("$tag: $it${System.lineSeparator()}")
        }
    }

    companion object {

        private val TARGET_TAGS = setOf(
                ExifInterface.TAG_ARTIST,
                ExifInterface.TAG_BITS_PER_SAMPLE,
                ExifInterface.TAG_BODY_SERIAL_NUMBER,
                ExifInterface.TAG_BRIGHTNESS_VALUE,
                ExifInterface.TAG_CAMERA_OWNER_NAME,
                ExifInterface.TAG_CFA_PATTERN,
                ExifInterface.TAG_COLOR_SPACE,
                ExifInterface.TAG_COMPONENTS_CONFIGURATION,
                ExifInterface.TAG_COMPRESSION,
                ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
                ExifInterface.TAG_CONTRAST,
                ExifInterface.TAG_COPYRIGHT,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_EXIF_VERSION,
                ExifInterface.TAG_GPS_SATELLITES,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_SUBJECT_LOCATION,
                ExifInterface.TAG_SATURATION,
                ExifInterface.TAG_SHARPNESS,
                ExifInterface.TAG_SOFTWARE,
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_IMAGE_DESCRIPTION
        )

    }

}