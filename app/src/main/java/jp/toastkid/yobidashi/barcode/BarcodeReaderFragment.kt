/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.camera.PreviewCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentBarcodeReaderBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.ExternalFileAssignment
import jp.toastkid.yobidashi.libs.view.DraggableTouchListener
import jp.toastkid.yobidashi.search.SearchAction
import timber.log.Timber
import java.io.FileOutputStream

/**
 * Barcode reader function fragment.
 *
 * @author toastkidjp
 */
class BarcodeReaderFragment : Fragment() {

    /**
     * Data Binding object.
     */
    private var binding: FragmentBarcodeReaderBinding? = null

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * For showing barcode reader result.
     */
    private lateinit var resultPopup: BarcodeReaderResultPopup

    /**
     * Required permission for this fragment(and function).
     */
    private val permission = Manifest.permission.CAMERA

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        resultPopup = BarcodeReaderResultPopup(requireContext())

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceApplier = PreferenceApplier(requireContext())

        binding?.fragment = this
        binding?.toolbar?.let { toolbar ->
            activity?.setTitle(R.string.title_camera)
            toolbar.inflateMenu(R.menu.camera)
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }

            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { activity?.finish() }
        }

        if (isNotGranted()) {
            requestPermissions(arrayOf(permission), 1)
            return
        }

        val requireActivity = requireActivity()

        ViewModelProviders.of(requireActivity).get(BarcodeReaderResultPopupViewModel::class.java)
                .also {
                    it.clip.observe(requireActivity, Observer { text -> clip(text) })
                    it.share.observe(requireActivity, Observer { text ->
                        startActivity(IntentFactory.makeShare(text))
                    })
                    it.open.observe(requireActivity, Observer { text ->
                        SearchAction(
                                requireActivity,
                                preferenceApplier.getDefaultSearchEngine(),
                                text
                        ).invoke()
                    })
                }

        initializeFab()
        startDecode()
    }

    /**
     * Return is granted required permission.
     *
     * @return If is granted camera permission, return true
     */
    private fun isNotGranted() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && activity?.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED

    /**
     * Invoke click menu action.
     *
     * @param item [MenuItem]
     * @return This function always return true
     */
    private fun clickMenu(item: MenuItem) = when (item.itemId) {
        R.id.reset_fab_position -> {
            binding?.camera?.also {
                it.translationX = 0f
                it.translationY = 0f
                preferenceApplier.clearCameraFabPosition()
            }
            true
        }
        R.id.menu_exit -> {
            activity?.moveTaskToBack(true)
            true
        }
        R.id.menu_close -> {
            activity?.finish()
            true
        }
        else -> true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeFab() {
        val draggableTouchListener = DraggableTouchListener()
        draggableTouchListener.setCallback(object : DraggableTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                preferenceApplier.setNewCameraFabPosition(x, y)
            }
        })
        binding?.camera?.setOnTouchListener(draggableTouchListener)

        binding?.camera?.also {
            val position = preferenceApplier.cameraFabPosition() ?: return@also
            it.animate()
                    .x(position.first)
                    .y(position.second)
                    .setDuration(10)
                    .start()
        }
    }

    /**
     * Start decode.
     */
    private fun startDecode() {
        binding?.barcodeView?.decodeContinuous(object : BarcodeCallback {

            override fun barcodeResult(barcodeResult: BarcodeResult) {
                val text = barcodeResult.text
                if (TextUtils.equals(text, resultPopup.currentText())) {
                    return
                }
                showResult(text)
            }

            override fun possibleResultPoints(list: List<ResultPoint>) = Unit
        })
    }

    /**
     * Copy result text to clipboard.
     *
     * @param text Result text
     */
    private fun clip(text: String) {
        binding?.root?.let { snackbarParent ->
            Clipboard.clip(snackbarParent.context, text)
            Toaster.snackShort(snackbarParent, text, preferenceApplier.colorPair())
        }
    }

    fun camera() {
        val barcodeView = binding?.barcodeView ?: return

        barcodeView.barcodeView?.cameraInstance?.requestPreview(object : PreviewCallback {
            override fun onPreview(sourceData: SourceData?) {
                val output = ExternalFileAssignment().assignFile(
                        requireContext(),
                        "shoot_${System.currentTimeMillis()}.png"
                )

                sourceData?.cropRect = getRect()
                sourceData?.bitmap?.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        FileOutputStream(output)
                )

                Toaster.snackShort(
                        barcodeView,
                        "Camera saved: ${output.absolutePath}",
                        preferenceApplier.colorPair()
                )
            }

            private fun getRect(): Rect {
                val rect = Rect()
                activity?.windowManager?.defaultDisplay?.getRectSize(rect)
                return rect
            }

            override fun onPreviewError(e: Exception?) {
                Timber.e(e)
            }

        })
    }

    /**
     * Show result with snackbar.
     *
     * @param text [String]
     */
    private fun showResult(text: String) {
        binding?.root?.let { resultPopup.show(it, text) }
    }


    override fun onResume() {
        super.onResume()
        binding?.barcodeView?.resume()
        val colorPair = preferenceApplier.colorPair()
        binding?.toolbar?.setTitleTextColor(colorPair.fontColor())
        resultPopup.onResume(colorPair)
    }

    override fun onPause() {
        super.onPause()
        binding?.barcodeView?.pause()
        resultPopup.hide()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDecode()
            return
        }

        activity?.let {
            Toaster.tShort(it, R.string.message_requires_permission_camera)
            it.finish()
        }
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_barcode_reader
    }
}