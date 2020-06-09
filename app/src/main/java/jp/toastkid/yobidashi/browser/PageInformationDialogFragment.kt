package jp.toastkid.yobidashi.browser

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Method object of displaying page information dialog.
 *
 * @author toastkidjp
 */
internal class PageInformationDialogFragment: DialogFragment() {

    private var favicon: Bitmap? = null

    private var title: String? = null

    private var url: String? = null

    private val imageCache = ImageCache()

    private val disposables: Job by lazy { Job() }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        favicon = args?.getParcelable<Bitmap?>("favicon")
        title = args?.getString("title")
        url = args?.getString("url")
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (url.isNullOrBlank()) {
            return super.onCreateDialog(savedInstanceState)
        }

        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val contentView = LayoutInflater.from(activityContext)
                .inflate(R.layout.content_dialog_share_barcode, null)

        contentView.findViewById<TextView>(R.id.title).text = "Title: $title"
        contentView.findViewById<TextView>(R.id.url).text = "URL: $url"

        val imageView = contentView.findViewById<ImageView>(R.id.barcode)

        CoroutineScope(Dispatchers.Main).launch(disposables) {
            val bitmap = withContext(Dispatchers.IO) { encodeBitmap() }
            setBitmap(bitmap, imageView, contentView)
        }

        val builder = AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_menu_page_information)
                .setView(contentView)
                .setNeutralButton("Clip URL") { d, _ -> clipUrl(d) }
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
        if (favicon != null) {
            builder.setIcon(BitmapDrawable(activityContext.resources, favicon))
        }
        return builder.create()
    }

    private fun encodeBitmap() = BarcodeEncoder()
            .encodeBitmap(url, BarcodeFormat.QR_CODE, BARCODE_SIZE, BARCODE_SIZE)

    private fun setBitmap(bitmap: Bitmap, imageView: ImageView, contentView: View) {
        val context = imageView.context ?: return
        imageView.setImageBitmap(bitmap)
        imageView.visibility = View.VISIBLE
        contentView.findViewById<View>(R.id.share).setOnClickListener {
            val uri = FileProvider.getUriForFile(
                    context,
                     "${BuildConfig.APPLICATION_ID}.fileprovider",
                    imageCache.saveBitmap(context.cacheDir, bitmap).absoluteFile
            )
            try {
                context.startActivity(IntentFactory.shareImage(uri))
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
            }
        }
    }

    /**
     * Copy URL to Clipboard.
     *
     * @param d
     */
    private fun clipUrl(d: DialogInterface) {
        val appContext = context ?: return
        url?.also { Clipboard.clip(appContext, it) }

        Toaster.tShort(
                appContext,
                "It has copied URL to clipboard.$lineSeparator$url"
        )
        d.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        disposables.cancel()
        super.onDismiss(dialog)
    }

    override fun onCancel(dialog: DialogInterface) {
        disposables.cancel()
        super.onCancel(dialog)
    }

    companion object {

        /**
         * Line separator.
         */
        private val lineSeparator = System.getProperty("line.separator")

        /**
         * Barcode size.
         */
        private const val BARCODE_SIZE = 400

    }
}
