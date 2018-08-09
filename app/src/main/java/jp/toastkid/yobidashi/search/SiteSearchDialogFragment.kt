package jp.toastkid.yobidashi.search

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivity
import java.util.*

/**
 * In site search with Google.
 *
 * @author toastkidjp
 */
class SiteSearchDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val textInputLayout = TextInputs.make(activityContext)
        TextInputs.setEmptyAlert(textInputLayout)

        val dialog = AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_site_search_by_google)
                .setView(textInputLayout)
                .setPositiveButton(R.string.title_search_action) { d, i ->
                    textInputLayout.editText?.text?.let { doAction(it.toString()) }
                    d.dismiss()
                }
                .create()
        textInputLayout.editText?.let { editText ->
            editText.hint = activityContext.getString(R.string.hint_please_input)
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (editText.text.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    return@setOnEditorActionListener false
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doAction(editText.text.toString())
                    dialog.dismiss()
                }
                true
            }
        }

        textInputLayout.requestFocus()
        return dialog
    }

    /**
     * Do search action with [WebView].
     *
     * @param query
     */
    private fun doAction(query: String) {
        val context: Context = context ?: return
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }
        startActivity(
                MainActivity.makeBrowserIntent(
                        context,
                        makeUrl(arguments?.getString(KEY_URL), query).toUri()
                )
        )
    }

    /**
     * Make URL.
     *
     * @param url
     * @param rawQuery
     * @return Search result URL
     */
    private fun makeUrl(url: String?, rawQuery: String): String =
            Formatter().format(FORMAT, url?.toUri()?.host, Uri.encode(rawQuery)).toString()

    companion object {

        /**
         * Site search URL format.
         */
        private const val FORMAT = "https://www.google.com/search?as_dt=i&as_sitesearch=%s&as_q=%s"

        private const val KEY_URL = "url"

        fun makeWithUrl(url: String?) = SiteSearchDialogFragment()
                .also { it.arguments = bundleOf(KEY_URL to url) }
    }
}
