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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
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

    private val disposables = CompositeDisposable()

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        favicon = args?.getParcelable<Bitmap?>("favicon")
        title = args?.getString("title")
        url = args?.getString("url")
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val contentView = LayoutInflater.from(activityContext)
                .inflate(R.layout.content_dialog_share_barcode, null)

        contentView.findViewById<TextView>(R.id.title).text = "Title: $title"
        contentView.findViewById<TextView>(R.id.url).text = "URL: $url"

        val imageView = contentView.findViewById<ImageView>(R.id.barcode)

        Single.fromCallable { encodeBitmap() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ setBitmap(it, imageView, contentView) }, Timber::e)
                .addTo(disposables)

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
                    ImageCache.saveBitmap(context, bitmap).absoluteFile
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

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
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
