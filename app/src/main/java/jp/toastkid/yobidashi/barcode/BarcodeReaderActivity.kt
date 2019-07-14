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
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.camera.PreviewCallback
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBarcodeReaderBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.storage.ExternalFileAssignment
import jp.toastkid.yobidashi.search.SearchAction
import timber.log.Timber
import java.io.FileOutputStream

/**
 * Barcode reader activity.
 *
 * @author toastkidjp
 */
class BarcodeReaderActivity : BaseActivity() {

    /**
     * Data Binding object.
     */
    private var binding: ActivityBarcodeReaderBinding? = null

    /**
     * Animation of slide up bottom.
     */
    private val slideUpBottom by lazy { AnimationUtils.loadAnimation(this, R.anim.slide_up) }

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this
        binding?.toolbar?.let {
            initToolbar(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            return
        }

        binding?.camera?.setOnTouchListener(object : View.OnTouchListener {

            private val CLICK_DRAG_TOLERANCE = 10

            private var downRawX = 0f

            private var downRawY = 0f

            private var dX = 0f

            private var dY = 0f

            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                if (view == null || motionEvent == null) {
                    return false
                }
                val action = motionEvent.action
                return when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        downRawX = motionEvent.rawX
                        downRawY = motionEvent.rawY
                        dX = view.x - downRawX
                        dY = view.y - downRawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val viewWidth = view.width
                        val viewHeight = view.height

                        val viewParent = view.parent as View
                        val parentWidth = viewParent.width.toFloat()
                        val parentHeight = viewParent.height.toFloat()

                        var newX = motionEvent.rawX + dX
                        newX = Math.max(0f, newX)
                        newX = Math.min(parentWidth - viewWidth, newX)

                        var newY = motionEvent.rawY + dY
                        newY = Math.max(0f, newY)
                        newY = Math.min(parentHeight - viewHeight, newY)

                        view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val upRawX = motionEvent.rawX
                        val upRawY = motionEvent.rawY

                        val upDX = upRawX - downRawX
                        val upDY = upRawY - downRawY

                        if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                            return binding?.camera?.performClick() ?: false
                        }
                        return true
                    }
                    else -> binding?.camera?.onTouchEvent(motionEvent) ?: false
                }
            }
        })
        startDecode()
    }

    /**
     * Start decode.
     */
    private fun startDecode() {
        binding?.barcodeView?.decodeContinuous(object : BarcodeCallback {

            override fun barcodeResult(barcodeResult: BarcodeResult) {
                val text = barcodeResult.text
                if (TextUtils.equals(text, getResultText())) {
                    return
                }
                @Suppress("UsePropertyAccessSyntax")
                binding?.result?.setText(text)
                showResult()
            }

            override fun possibleResultPoints(list: List<ResultPoint>) = Unit
        })
    }

    /**
     * Copy result text to clipboard.
     */
    fun clip(snackbarParent: View) {
        getResultText()?.let {
            Clipboard.clip(this, it)
            Toaster.snackShort(snackbarParent, it, colorPair())
        }
    }

    /**
     * Share result text.
     */
    fun share() {
        getResultText()?.let { startActivity(IntentFactory.makeShare(it)) }
    }

    /**
     * Open result text with browser.
     */
    fun open() {
        getResultText()?.let {
            SearchAction(this, preferenceApplier.getDefaultSearchEngine(), it).invoke()
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
                Toaster.snackShort(barcodeView, "Camera saved: ${output.absolutePath}", colorPair())
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
     * Get result text.
     */
    private fun getResultText(): String? = binding?.result?.text?.toString()

    /**
     * Show result with snackbar.
     */
    private fun showResult() {
        binding?.resultArea?.let {
            if (it.visibility != View.VISIBLE) {
                it.visibility = View.VISIBLE
            }
            it.startAnimation(slideUpBottom)
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.barcodeView?.resume()
        val colorPair = colorPair()
        binding?.toolbar?.setTitleTextColor(colorPair.fontColor())
        binding?.let {
            it.resultArea.setBackgroundColor(colorPair.bgColor())
            Colors.setColors(it.clip, colorPair)
            Colors.setColors(it.share, colorPair)
            Colors.setColors(it.open, colorPair)
            Colors.setColors(it.result, colorPair)
        }
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

    @StringRes
    override fun titleId(): Int = R.string.title_camera

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
