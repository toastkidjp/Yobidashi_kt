/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.FileInputStream

class ImageDetailFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = context ?: return null
        val textView = TextView(context)
        val imageUri = arguments?.getParcelable<Uri>(KEY_EXTRA_IMAGE_URI) ?: return null
        val inputStream = FileInputStream(File(imageUri.toString()))
        val exifInterface = ExifInterface(inputStream)
        val lineSeparator = System.lineSeparator()
        val stringBuilder = StringBuilder()
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_ARTIST)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_BITS_PER_SAMPLE)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_BODY_SERIAL_NUMBER)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_BRIGHTNESS_VALUE)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_CAMERA_OWNER_NAME)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_CFA_PATTERN)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_COLOR_SPACE)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_COMPONENTS_CONFIGURATION)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_COMPRESSION)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_CONTRAST)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_COPYRIGHT)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_DATETIME)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_DATETIME_DIGITIZED)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_EXIF_VERSION)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_GPS_SATELLITES)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_SATURATION)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_SHARPNESS)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_SOFTWARE)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_ORIENTATION)
        extractAttribute(stringBuilder, exifInterface, ExifInterface.TAG_IMAGE_DESCRIPTION)
        textView.setText(
                "gpsDateTime: ${exifInterface.gpsDateTime}$lineSeparator" +
                        "isFlipped: ${exifInterface.isFlipped}$lineSeparator" +
                        "gpsDateTime: ${exifInterface.gpsDateTime}$lineSeparator" +
                        "latLong: ${exifInterface.latLong}$lineSeparator" +
                        "isThumbnailCompressed: ${exifInterface.isThumbnailCompressed}$lineSeparator" +
                        "rotationDegrees: ${exifInterface.rotationDegrees}$lineSeparator${stringBuilder.toString()}"
        )
        inputStream.close()
        return textView
    }

    private fun extractAttribute(stringBuilder: StringBuilder, exifInterface: ExifInterface, tag: String) {
        exifInterface.getAttribute(tag)?.let {
            stringBuilder.append("$tag: $it${System.lineSeparator()}")
        }
    }

    companion object {

        private const val KEY_EXTRA_IMAGE_URI = "image_uri"

        fun withImageUri(imageUri: Uri): DialogFragment =
                ImageDetailFragment().also {
                    it.arguments = bundleOf(KEY_EXTRA_IMAGE_URI to imageUri)
                }

    }

}