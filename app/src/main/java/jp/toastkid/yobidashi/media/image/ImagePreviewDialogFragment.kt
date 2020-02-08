/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogImagePreviewBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import timber.log.Timber
import java.io.File

/**
 * @author toastkidjp
 */
class ImagePreviewDialogFragment  : DialogFragment() {

    private lateinit var binding: DialogImagePreviewBinding

    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)

        val activityContext = context
                ?: return super.onCreateDialog(savedInstanceState)

        val path = arguments?.getString(KEY_IMAGE)
                ?: return super.onCreateDialog(savedInstanceState)

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(activityContext),
                R.layout.dialog_image_preview,
                null,
                false
        )

        binding.dialog = this

        val displayMetrics = resources.displayMetrics
        val layoutParams = binding.photo.layoutParams
        layoutParams.width = displayMetrics.widthPixels
        layoutParams.height = displayMetrics.heightPixels
        binding.photo.layoutParams = layoutParams

        Maybe.fromCallable {
            ImageLoader.loadBitmap(
                    activityContext,
                    Uri.parse(File(path).toURI().toString())
            )
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            binding.photo.setImageBitmap(it)
                        },
                        Timber::e
                )
                .addTo(disposables)

        binding.photo.maximumScale = 100f

        return AlertDialog.Builder(activityContext)
                .setView(binding.root)
                .create()
                .also {
                    it.window?.also { window ->
                        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                }
    }

    fun reverse() {
        val current = binding.photo.drawable.toBitmap()
        val horizontalFlipped = Bitmap.createBitmap(
                current,
                0,
                0,
                current.getWidth(),
                current.getHeight(),
                horizontalMatrix,
                false
        )
        binding.photo.setImageBitmap(horizontalFlipped)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        disposables.clear()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        disposables.dispose()
    }

    companion object {

        private const val KEY_IMAGE = "image"

        private val horizontalMatrix = Matrix().also { it.preScale(-1f, 1f) }

        fun withImage(image: Image) =
                ImagePreviewDialogFragment()
                        .also {
                            it.arguments = bundleOf(KEY_IMAGE to image.path)
                        }
    }

}
