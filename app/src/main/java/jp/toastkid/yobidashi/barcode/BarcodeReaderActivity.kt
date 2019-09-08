package jp.toastkid.yobidashi.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.camera.PreviewCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBarcodeReaderBinding
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
 * Barcode reader activity.
 * TODO Clean up code.
 * @author toastkidjp
 */
class BarcodeReaderActivity : AppCompatActivity() {

    /**
     * Data Binding object.
     */
    private var binding: ActivityBarcodeReaderBinding? = null

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var resultPopup: BarcodeReaderResultPopup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this
        binding?.toolbar?.let { toolbar ->
            setTitle(R.string.title_camera)
            toolbar.inflateMenu(R.menu.camera)
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }

            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            return
        }

        resultPopup = BarcodeReaderResultPopup(this)

        ViewModelProviders.of(this).get(BarcodeReaderResultPopupViewModel::class.java)
                .also {
                    it.clip.observe(this, Observer { text -> clip(text) })
                    it.share.observe(this, Observer { text ->
                        startActivity(IntentFactory.makeShare(text))
                    })
                    it.open.observe(this, Observer { text ->
                        SearchAction(this, preferenceApplier.getDefaultSearchEngine(), text).invoke()
                    })
                }

        initializeFab()
        startDecode()
    }

    fun clickMenu(item: MenuItem) = when (item.itemId) {
        R.id.reset_fab_position -> {
            binding?.camera?.also {
                it.translationX = 0f
                it.translationY = 0f
                preferenceApplier.clearCameraFabPosition()
            }
            true
        }
        R.id.menu_exit -> {
            moveTaskToBack(true)
            true
        }
        R.id.menu_close -> {
            finish()
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
     */
    private fun clip(text: String) {
        Clipboard.clip(this, text)
        binding?.root?.let { snackbarParent ->
            Toaster.snackShort(snackbarParent, text, preferenceApplier.colorPair())
        }
    }

    fun camera() {
        val barcodeView = binding?.barcodeView ?: return

        barcodeView.barcodeView?.cameraInstance?.requestPreview(object : PreviewCallback {
            override fun onPreview(sourceData: SourceData?) {
                val output = ExternalFileAssignment().assignFile(
                        this@BarcodeReaderActivity,
                        "shoot_${System.currentTimeMillis()}.png"
                )

                sourceData?.cropRect = getRect()
                sourceData?.bitmap?.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(output))

                Toaster.snackShort(
                        barcodeView,
                        "Camera saved: ${output.absolutePath}",
                        preferenceApplier.colorPair()
                )
            }

            private fun getRect(): Rect {
                val rect = Rect()
                windowManager.defaultDisplay.getRectSize(rect)
                return rect
            }

            override fun onPreviewError(e: Exception?) {
                Timber.e(e)
            }

        })
    }

    /**
     * Show result with snackbar.
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
        Toaster.tShort(this, R.string.message_requires_permission_camera)
        finish()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_barcode_reader

        /**
         * Make this activity's intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BarcodeReaderActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
