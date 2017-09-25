package jp.toastkid.yobidashi.browser

import android.content.Context
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import timber.log.Timber

/**
 * Action of sharing URL by barcode.
 *
 * @author toastkidjp
 */
internal object SharingUrlByBarcode {

    /**
     * Invoke action.
     *
     * @param context
     * @param url
     */
    operator fun invoke(context: Context, url: String) {
        val imageView = ImageView(context)
        try {
            val bitmap = BarcodeEncoder()
                    .encodeBitmap(url, BarcodeFormat.QR_CODE, 400, 400)
            imageView.setImageBitmap(bitmap)
            AlertDialog.Builder(context)
                    .setTitle(R.string.title_share_by_code)
                    .setView(imageView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.share) { d, i ->
                        val uri = FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".fileprovider",
                                ImageCache.saveBitmap(context, bitmap).absoluteFile
                        )
                        context.startActivity(IntentFactory.shareImage(uri))
                        d.dismiss()
                    }
                    .show()
        } catch (e: WriterException) {
            Timber.e(e)
            Toaster.tShort(context, e.message.orEmpty())
        }
    }
}