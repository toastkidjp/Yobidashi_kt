package jp.toastkid.yobidashi.barcode

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBarcodeReaderBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.main.MainActivity

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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        binding = DataBindingUtil.setContentView<ActivityBarcodeReaderBinding>(this, LAYOUT_ID)
        binding?.activity = this
        binding?.toolbar?.let {
            setSupportActionBar(it)
            initToolbar(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            return
        }
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
                binding?.result?.setText(text)
                showResult(text)
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
    fun share(ignored: View) {
        getResultText()?.let { startActivity(IntentFactory.makeShare(it)) }
    }

    /**
     * Open result text with browser.
     */
    fun open(ignored: View) {
        getResultText()?.let {
            startActivity(MainActivity.makeBrowserIntent(this, Uri.parse(it)))
        }
    }

    /**
     * Get result text.
     */
    private fun getResultText(): String? = binding?.result?.getText()?.toString()

    /**
     * Show result with snackbar.
     *
     * @param text
     */
    private fun showResult(text: String) {
        binding?.resultArea?.let {
            if (it.visibility != View.VISIBLE) {
                it.visibility = View.VISIBLE
            }
            it.startAnimation(slideUpBottom)
        }
    }

    public override fun onResume() {
        super.onResume()
        binding?.barcodeView?.resume()
        val colorPair = colorPair()
        binding?.toolbar?.setTitleTextColor(colorPair.fontColor())
        binding?.let {
            it.resultArea.setBackgroundColor(colorPair.bgColor())
            Colors.setBgAndText(it.clip, colorPair)
            Colors.setBgAndText(it.share, colorPair)
            Colors.setBgAndText(it.open, colorPair)
            Colors.setBgAndText(it.result, colorPair)
        }
    }

    public override fun onPause() {
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

    override fun titleId(): Int = R.string.title_camera

    companion object {

        /**
         * Layout ID.
         */
        private val LAYOUT_ID = R.layout.activity_barcode_reader

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
