/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.app.Dialog
import android.content.ContentResolver
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogImagePreviewBinding
import jp.toastkid.yobidashi.media.image.Image
import jp.toastkid.yobidashi.media.image.preview.attach.AttachToThisAppBackgroundUseCase

/**
 * @author toastkidjp
 */
class ImagePreviewDialogFragment  : DialogFragment() {

    private lateinit var binding: DialogImagePreviewBinding

    private lateinit var contentResolver: ContentResolver

    private lateinit var contentViewModel: ContentViewModel

    private var pathFinder: () -> String? = { null }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)

        val activityContext = context
                ?: return super.onCreateDialog(savedInstanceState)

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(activityContext),
                LAYOUT_ID,
                null,
                false
        )

        binding.dialog = this
        binding.moduleEdit.dialog = this

        fitPhotoView()

        applyColorToButtons()

        val adapter = Adapter()
        binding.photo.adapter = adapter
        LinearSnapHelper().attachToRecyclerView(binding.photo)

        pathFinder = {
            findLayoutManager()?.let {
                adapter.getPath(it.findFirstVisibleItemPosition())
            }
        }

        contentViewModel = ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)

        val viewModel =
                ViewModelProvider(this).get(ImagePreviewFragmentViewModel::class.java)
        binding.moduleEdit.colorFilterUseCase = ColorFilterUseCase(viewModel)
        viewModel.colorFilter.observe(this, Observer {
            findCurrentImageView()?.colorFilter = it
        })

        initializeContrastSlider()
        initializeAlphaSlider()

        binding.moduleEdit.imageRotationUseCase = ImageRotationUseCase(viewModel) {
            findCurrentImageView()?.drawable?.toBitmap()
        }

        viewModel.bitmap.observe(this, Observer {
            findCurrentImageView()?.setImageBitmap(it)
        })

        contentResolver = binding.root.context.contentResolver

        val images: List<Image> = arguments?.getSerializable(KEY_IMAGE) as? List<Image>
                ?: return super.onCreateDialog(savedInstanceState)
        adapter.setImages(images)
        adapter.notifyDataSetChanged()
        val position = arguments?.getInt(KEY_POSITION) ?: 0
        binding.photo.scrollToPosition(position)

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

    private fun findCurrentImageView() =
            findLayoutManager()?.let {
                it.findViewByPosition(it.findFirstVisibleItemPosition()) as? ImageView
            }

    private fun findLayoutManager() = binding.photo.layoutManager as? LinearLayoutManager

    private fun initializeContrastSlider() {
        binding.moduleEdit.contrast.progress = 50
        binding.moduleEdit.contrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }

                binding.moduleEdit.colorFilterUseCase?.applyContrast(((progress - 50).toFloat() / 70f))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })
    }

    private fun initializeAlphaSlider() {
        binding.moduleEdit.alpha.progress = 50
        binding.moduleEdit.alpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }

                binding.moduleEdit.colorFilterUseCase?.applyAlpha(((progress - 50).toFloat() / 100f))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })
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
        binding.moduleEdit.reverse.setColorFilter(fontColor)
        binding.moduleEdit.rotateLeft.setColorFilter(fontColor)
        binding.moduleEdit.rotateRight.setColorFilter(fontColor)
        binding.moduleEdit.setTo.setColorFilter(fontColor)
        binding.moduleEdit.edit.setColorFilter(fontColor)
        binding.visibilitySwitch.setColorFilter(fontColor)
        binding.close.setColorFilter(fontColor)
    }

    fun setTo() {
        val context = context ?: return
        val uri = pathFinder()?.toUri() ?: return
        val image = findCurrentImageView()?.drawable?.toBitmap() ?: return
        AttachToThisAppBackgroundUseCase(contentViewModel).invoke(context, uri, image)
    }

    fun edit() {
        EditorChooserInvokingUseCase(
                pathFinder,
                { contentViewModel.snackShort(R.string.message_cannot_launch_app) },
                { binding.root.context.startActivity(it) }
        ).invoke(requireContext())
    }

    fun switchVisibility() {
        binding.moduleEdit.root.isVisible = !binding.moduleEdit.root.isVisible
        binding.visibilitySwitch.setImageResource(
                if (binding.moduleEdit.root.isVisible) R.drawable.ic_down else R.drawable.ic_up
        )
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.dialog_image_preview

        private const val KEY_IMAGE = "image"

        private const val KEY_POSITION = "position"

        fun withImages(image: Collection<Image>, position: Int) =
                ImagePreviewDialogFragment()
                        .also {
                            it.arguments = bundleOf(
                                    KEY_IMAGE to image,
                                    KEY_POSITION to position
                            )
                        }
    }

}
