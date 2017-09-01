package jp.toastkid.jitte.barcode

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.widget.ImageView

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

import jp.toastkid.jitte.R
import timber.log.Timber

/**
 * Instant barcode generator.
 * Initialize with context.
 * 
 * @param context
 * 
 * @author toastkidjp
 */
class InstantBarcodeGenerator(
        /** Context  */
        private val context: Context) {

    /**
     * Launch generator.
     */
    operator fun invoke() {
        val editText = EditText(context)
        editText.hint = context.getString(R.string.hint_generate_barcode)
        AlertDialog.Builder(context)
                .setTitle(R.string.title_instant_barcode)
                .setView(editText)
                .setCancelable(true)
                .setPositiveButton(R.string.generate) { d, i ->
                    val text = editText.text.toString()
                    if (text.isNotEmpty()) {
                        showResultDialog(text)
                    }
                }
                .show()
    }

    /**
     * Show result dialog.
     * @param string
     */
    private fun showResultDialog(string: String) {
        val imageView = ImageView(context)
        try {
            imageView.setImageDrawable(
                    BitmapDrawable(context.resources,
                            BarcodeEncoder()
                                    .encodeBitmap(string, BarcodeFormat.QR_CODE, 400, 400)))
        } catch (e: WriterException) {
            Timber.e(e)
        }

        AlertDialog.Builder(context)
                .setTitle(string)
                .setView(imageView)
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { d, i -> d.dismiss() }
                .show()
    }
}
