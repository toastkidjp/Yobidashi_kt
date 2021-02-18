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
        extractAttribute(exifInterface, ExifInterface.TAG_ARTIST)
        extractAttribute(exifInterface, ExifInterface.TAG_BITS_PER_SAMPLE)
        extractAttribute(exifInterface, ExifInterface.TAG_BODY_SERIAL_NUMBER)
        extractAttribute(exifInterface, ExifInterface.TAG_BRIGHTNESS_VALUE)
        extractAttribute(exifInterface, ExifInterface.TAG_CAMERA_OWNER_NAME)
        extractAttribute(exifInterface, ExifInterface.TAG_CFA_PATTERN)
        extractAttribute(exifInterface, ExifInterface.TAG_COLOR_SPACE)
        extractAttribute(exifInterface, ExifInterface.TAG_COMPONENTS_CONFIGURATION)
        extractAttribute(exifInterface, ExifInterface.TAG_COMPRESSION)
        extractAttribute(exifInterface, ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL)
        extractAttribute(exifInterface, ExifInterface.TAG_CONTRAST)
        extractAttribute(exifInterface, ExifInterface.TAG_COPYRIGHT)
        extractAttribute(exifInterface, ExifInterface.TAG_DATETIME)
        extractAttribute(exifInterface, ExifInterface.TAG_DATETIME_DIGITIZED)
        extractAttribute(exifInterface, ExifInterface.TAG_EXIF_VERSION)
        extractAttribute(exifInterface, ExifInterface.TAG_GPS_SATELLITES)
        extractAttribute(exifInterface, ExifInterface.TAG_SATURATION)
        extractAttribute(exifInterface, ExifInterface.TAG_SHARPNESS)
        extractAttribute(exifInterface, ExifInterface.TAG_SOFTWARE)
        extractAttribute(exifInterface, ExifInterface.TAG_ORIENTATION)
        extractAttribute(exifInterface, ExifInterface.TAG_IMAGE_DESCRIPTION)
        val content = stringBuilder.toString()
        stringBuilder.setLength(0)
        return content
    }

    private fun extractAttribute(exifInterface: androidx.exifinterface.media.ExifInterface, tag: String) {
        exifInterface.getAttribute(tag)?.let {
            stringBuilder.append("$tag: $it${System.lineSeparator()}")
        }
    }
    
}