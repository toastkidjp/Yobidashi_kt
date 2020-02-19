/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
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
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.image.Image
import timber.log.Timber
import java.io.File

/**
 * @author toastkidjp
 */
class ImagePreviewDialogFragment  : DialogFragment() {

    private lateinit var binding: DialogImagePreviewBinding

    private var path: String? = null

    private val imageEditChooserFactory = ImageEditChooserFactory()

    private val rotatedBitmapFactory = RotatedBitmapFactory()

    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)

        val activityContext = context
                ?: return super.onCreateDialog(savedInstanceState)

        path = arguments?.getString(KEY_IMAGE)
                ?: return super.onCreateDialog(savedInstanceState)

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(activityContext),
                LAYOUT_ID,
                null,
                false
        )

        binding.dialog = this

        fitPhotoView()

        applyColorToButtons()

        loadImageAsync(activityContext, path)

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

    private fun fitPhotoView() {
        val displayMetrics = resources.displayMetrics
        val layoutParams = binding.photo.layoutParams
        layoutParams.width = displayMetrics.widthPixels
        layoutParams.height = displayMetrics.heightPixels
        binding.photo.layoutParams = layoutParams
    }

    private fun applyColorToButtons() {
        val fontColor = PreferenceApplier(binding.root.context).fontColor
        binding.reverse.setColorFilter(fontColor)
        binding.rotateLeft.setColorFilter(fontColor)
        binding.rotateRight.setColorFilter(fontColor)
        binding.edit.setColorFilter(fontColor)
    }

    private fun loadImageAsync(activityContext: Context, path: String?) {
        if (path == null) {
            return
        }

        Maybe.fromCallable {
            ImageLoader.loadBitmap(
                    activityContext,
                    Uri.parse(File(path).toURI().toString())
            )
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        binding.photo::setImageBitmap,
                        Timber::e
                )
                .addTo(disposables)
    }

    fun edit() {
        if (path == null) {
            Toaster.snackShort(
                    binding.root,
                    R.string.message_cannot_launch_app,
                    PreferenceApplier(binding.root.context).colorPair()
            )
            return
        }

        try {
            binding.root.context.startActivity(imageEditChooserFactory(requireContext(), path))
        } catch (e: ActivityNotFoundException) {
            Timber.w(e)
            Toaster.snackShort(
                    binding.root,
                    R.string.message_cannot_launch_app,
                    PreferenceApplier(binding.root.context).colorPair()
            )
        }
    }

    fun rotateLeft() {
        val bitmap = binding.photo.drawable.toBitmap()
        binding.photo.setImageBitmap(rotatedBitmapFactory.rotateLeft(bitmap))
    }

    fun rotateRight() {
        val bitmap = binding.photo.drawable.toBitmap()
        binding.photo.setImageBitmap(rotatedBitmapFactory.rotateRight(bitmap))
    }

    fun reverse() {
        val bitmap = binding.photo.drawable.toBitmap()
        binding.photo.setImageBitmap(rotatedBitmapFactory.reverse(bitmap))
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        disposables.clear()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        disposables.clear()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.dialog_image_preview

        private const val KEY_IMAGE = "image"

        fun withImage(image: Image) =
                ImagePreviewDialogFragment()
                        .also {
                            it.arguments = bundleOf(KEY_IMAGE to image.path)
                        }
    }

}
