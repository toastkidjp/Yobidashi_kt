package jp.toastkid.yobidashi.search

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Inputs
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

        textInputLayout.editText?.let { editText ->
            editText.hint = activityContext.getString(R.string.hint_please_input)
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (editText.text.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    return@setOnEditorActionListener false
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doAction(editText.text.toString())
                    dialog?.dismiss()
                }
                true
            }
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_site_search_by_google)
                .setIcon(R.drawable.ic_google)
                .setView(textInputLayout)
                .setPositiveButton(R.string.title_search_action) { d, _ ->
                    textInputLayout.editText?.text?.let { doAction(it.toString()) }
                    d.dismiss()
                }
                .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Inputs.showKeyboardForInputDialog(dialog?.window)
    }

    /**
     * Do search action with [WebView].
     *
     * @param query
     */
    private fun doAction(query: String?) {
        if (query.isNullOrBlank()) {
            return
        }
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

        /**
         * Extra key for URL string.
         */
        private const val KEY_URL = "url"

        /**
         * Make this fragment instance with URL string.
         *
         * @param url URL string.
         */
        fun makeWithUrl(url: String?) = SiteSearchDialogFragment()
                .also { it.arguments = bundleOf(KEY_URL to url) }
    }
}
