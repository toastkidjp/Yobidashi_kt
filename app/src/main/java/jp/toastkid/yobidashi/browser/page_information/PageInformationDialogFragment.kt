package jp.toastkid.yobidashi.browser.page_information

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import kotlinx.coroutines.Job

/**
 * Method object of displaying page information dialog.
 *
 * @author toastkidjp
 */
internal class PageInformationDialogFragment: DialogFragment() {

    private var favicon: Bitmap? = null

    private var title: String? = null

    private var url: String? = null

    private var cookie: String? = null

    private val disposables: Job by lazy { Job() }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        favicon = args?.getParcelable("favicon")
        title = args?.getString("title")
        url = args?.getString("url")
        cookie = args?.getString("cookie")
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (url.isNullOrBlank()) {
            return super.onCreateDialog(savedInstanceState)
        }

        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val contentView = LayoutInflater.from(activityContext)
                .inflate(R.layout.content_dialog_share_barcode, null)

        contentView.findViewById<TextView>(R.id.url).text = "URL: $url"

        contentView.findViewById<TextView>(R.id.cookie).text =
                "Cookie:$lineSeparator${cookie?.replace(";", ";$lineSeparator")}"

        BarcodePreparationUseCase().invoke(contentView, url)

        val builder = AlertDialog.Builder(activityContext)
                .setTitle(title)
                .setView(contentView)
                .setNeutralButton(R.string.button_clip_url) { d, _ -> clipText(url, d) }
                .setNegativeButton(R.string.button_clip_cookie) { d, _ -> clipText(cookie, d) }
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
        if (favicon != null) {
            builder.setIcon(BitmapDrawable(activityContext.resources, favicon))
        }
        return builder.create()
    }

    /**
     * Copy URL to Clipboard.
     *
     * @param copyText text for clipping (Nullable)
     * @param d [DialogInterface]
     */
    private fun clipText(copyText: String?, d: DialogInterface) {
        val appContext = context ?: return
        copyText?.also { Clipboard.clip(appContext, it) }

        Toaster.tShort(
                appContext,
                "It has copied URL to clipboard.$lineSeparator$copyText"
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

    }
}
