package jp.toastkid.jitte.barcode

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.webkit.URLUtil

import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult

import jp.toastkid.jitte.BaseActivity
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ActivityBarcodeReaderBinding
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.main.MainActivity

/**
 * Barcode reader activity.

 * @author toastkidjp
 */
class BarcodeReaderActivity : BaseActivity() {

    /** Data Binding object.  */
    private var binding: ActivityBarcodeReaderBinding? = null

    /** Previous snackbar.  */
    private var snackbar: Snackbar? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        binding = DataBindingUtil.setContentView<ActivityBarcodeReaderBinding>(this, LAYOUT_ID)
        setSupportActionBar(binding!!.toolbar)
        initToolbar(binding!!.toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
                return
            }
        }
        startDecode()
    }

    private fun startDecode() {
        binding!!.barcodeView.decodeContinuous(object : BarcodeCallback {

            internal var previousDecoded = ""

            override fun barcodeResult(barcodeResult: BarcodeResult) {
                val text = barcodeResult.text
                if (previousDecoded == text) {
                    return
                }
                previousDecoded = text

                if (snackbar != null) {
                    snackbar!!.dismiss()
                }
                showResult(text)
            }

            override fun possibleResultPoints(list: List<ResultPoint>) {
                // NOP.
            }
        })
    }

    /**
     * Show result with snackbar.
     * @param text
     */
    private fun showResult(text: String) {
        if (URLUtil.isHttpUrl(text) || URLUtil.isHttpsUrl(text)) {
            snackbar = Toaster.withAction(binding!!.root, text, R.string.display,
                    View.OnClickListener{ v ->
                        startActivity(MainActivity.makeBrowserIntent(
                                this@BarcodeReaderActivity, Uri.parse(text)))
                    },
                    colorPair()
            )
            return
        }
        snackbar = Toaster.snackIndefinite(binding!!.root, text, colorPair())
    }

    public override fun onResume() {
        super.onResume()
        binding!!.barcodeView.resume()
        binding!!.toolbar.setTitleTextColor(colorPair().fontColor())
    }

    public override fun onPause() {
        super.onPause()
        binding!!.barcodeView.pause()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDecode()
            return
        }
        Toaster.tShort(this, R.string.message_requires_permission_camera)
        finish()
    }

    override fun titleId(): Int {
        return R.string.title_camera
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_barcode_reader

        /**
         * Make this activity's intent.
         * @param context
         * *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BarcodeReaderActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
