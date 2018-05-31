package jp.toastkid.yobidashi.browser

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * Method object of displaying page information dialog.
 *
 * @author toastkidjp
 */
internal class PageInformationDialogFragment: DialogFragment() {

    private var favicon: Bitmap? = null

    private var title: String? = null

    private var url: String? = null

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        favicon = args?.getParcelable<Bitmap?>("favicon")
        title = args?.getString("title")
        url = args?.getString("url")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
                .setTitle(R.string.title_menu_page_information)
                .setMessage(makeMessage())
                .setCancelable(true)
                .setNeutralButton("Clip URL") { d, _ -> clipUrl(d) }
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
        if (favicon != null) {
            builder.setIcon(BitmapDrawable(context.resources, favicon))
        }
        return builder.create()
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

    /**
     * Make message.
     */
    private fun makeMessage(): String =
            "Title: $title${lineSeparator}URL: $url$lineSeparator"

    companion object {

        /**
         * Line separator.
         */
        private val lineSeparator = System.getProperty("line.separator")

    }
}
