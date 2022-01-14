/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.image.preview.detail

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = context ?: return null
        val textView = TextView(context)
        val imageUri = arguments?.getParcelable<Uri>(KEY_EXTRA_IMAGE_URI) ?: return null
        val inputStream = FileInputStream(File(imageUri.toString()))
        val exifInterface = ExifInterface(inputStream)

        textView.text = ExifInformationExtractorUseCase().invoke(exifInterface)
        inputStream.close()
        return textView
    }

    companion object {

        private const val KEY_EXTRA_IMAGE_URI = "image_uri"

        fun withImageUri(imageUri: Uri): DialogFragment =
                ImageDetailFragment().also {
                    it.arguments = bundleOf(KEY_EXTRA_IMAGE_URI to imageUri)
                }

    }

}