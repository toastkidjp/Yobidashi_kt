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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import jp.toastkid.yobidashi.libs.permission.RuntimePermissions
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.ExternalFileAssignment
import jp.toastkid.yobidashi.libs.view.DraggableTouchListener
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.search.SearchAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private var viewModel: BarcodeReaderResultPopupViewModel? = null

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * For showing barcode reader result.
     */
    private lateinit var resultPopup: BarcodeReaderResultPopup

    private var contentViewModel: ContentViewModel? = null

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

        if (isNotGranted()) {
            requestPermissions(arrayOf(permission), 1)
            return
        }

        val requireActivity = requireActivity()

        viewModel = ViewModelProvider(this).get(BarcodeReaderResultPopupViewModel::class.java)
        viewModel?.also {
            val viewLifecycleOwner = viewLifecycleOwner
            it.clip.observe(viewLifecycleOwner, Observer { text -> clip(text) })
            it.share.observe(viewLifecycleOwner, Observer { text ->
                startActivity(IntentFactory.makeShare(text))
                activity?.supportFragmentManager?.popBackStack()
            })
            it.open.observe(viewLifecycleOwner, Observer { text ->
                SearchAction(
                        requireActivity,
                        preferenceApplier.getDefaultSearchEngine(),
                        text
                ).invoke()
                activity?.supportFragmentManager?.popBackStack()
            })
        }

        resultPopup.setViewModel(viewModel)

        contentViewModel = ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)

        initializeFab()
        startDecode()

        setHasOptionsMenu(true)
    }

    /**
     * Return is granted required permission.
     *
     * @return If is granted camera permission, return true
     */
    private fun isNotGranted() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && activity?.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.camera, menu)
    }

    /**
     * Invoke click menu action.
     *
     * @param item [MenuItem]
     * @return This function always return true
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.reset_camera_fab_position -> {
            binding?.camera?.also {
                it.translationX = 0f
                it.translationY = 0f
                preferenceApplier.clearCameraFabPosition()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeFab() {
        val draggableTouchListener = DraggableTouchListener()
        draggableTouchListener.setCallback(object : DraggableTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                preferenceApplier.setNewCameraFabPosition(x, y)
            }
        })
        draggableTouchListener.setOnClick(object : DraggableTouchListener.OnClick {
            override fun onClick() {
                camera()
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

    private fun camera() {
        CoroutineScope(Dispatchers.Main).launch {
            val result =
                    RuntimePermissions(requireActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ?.receive()
            if (result?.granted == true) {
                invokeRequest()
                return@launch
            }

            contentViewModel?.snackShort(R.string.message_requires_permission_storage)
        }
    }

    private fun invokeRequest() {
        val barcodeView = binding?.barcodeView ?: return

        barcodeView.barcodeView?.cameraInstance?.requestPreview(object : PreviewCallback {
            override fun onPreview(sourceData: SourceData?) {
                val output = ExternalFileAssignment().assignFile(
                        requireContext(),
                        "shoot_${System.currentTimeMillis()}.png"
                )

                sourceData?.cropRect = getRect()
                val fileOutputStream = FileOutputStream(output)
                sourceData?.bitmap?.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        fileOutputStream
                )
                fileOutputStream.close()

                contentViewModel?.snackShort("Camera saved: ${output.absolutePath}")
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
        resultPopup.onResume(colorPair)
        colorPair.applyReverseTo(binding?.camera)
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
            parentFragmentManager.popBackStack()
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