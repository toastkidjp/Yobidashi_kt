package jp.toastkid.yobidashi.search.clip

import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.core.net.toUri
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.UrlFactory

/**
 * Search action with clipboard text.
 * Initialize with ClipboardManager, parent view, and color pair.
 *
 * @param cm For monitoring clipboard.
 * @param parent Use for showing snackbar.
 * @param colorPair Use for showing snackbar.
 * @param browserViewModel [BrowserViewModel].
 *
 * @author toastkidjp
 */
class SearchWithClip(
        private val cm: ClipboardManager,
        private val parent: View,
        private val colorPair: ColorPair,
        private val browserViewModel: BrowserViewModel?
) {

    /**
     * Last clopped epoch ms.
     */
    private var lastClipped: Long = 0L

    /**
     * [ClipboardManager.OnPrimaryClipChangedListener].
     */
    private val listener: ClipboardManager.OnPrimaryClipChangedListener by lazy {
        ClipboardManager.OnPrimaryClipChangedListener{
            if (isInvalidCondition() || NetworkChecker.isNotAvailable(parent.context)) {
                return@OnPrimaryClipChangedListener
            }
            lastClipped = System.currentTimeMillis()

            val firstItem = cm.primaryClip?.getItemAt(0)
                    ?: return@OnPrimaryClipChangedListener

            val text = firstItem.text
            if (text.isNullOrEmpty() || (Urls.isInvalidUrl(text.toString()) && LENGTH_LIMIT <= text.length)) {
                return@OnPrimaryClipChangedListener
            }

            val context = parent.context
            Toaster.snackLong(
                    parent,
                    context.getString(R.string.message_clip_search, text),
                    R.string.title_search_action,
                    View.OnClickListener { searchOrBrowse(context, text) },
                    colorPair
            )
        }
    }

    /**
     * If it is invalid condition, return true.
     *
     * @return If it is invalid condition, return true.
     */
    private fun isInvalidCondition(): Boolean {
        return (!PreferenceApplier(parent.context).enableSearchWithClip
                || !cm.hasPrimaryClip()
                || (System.currentTimeMillis() - lastClipped) < DISALLOW_INTERVAL_MS)
    }

    /**
     * Invoke action.
     */
    operator fun invoke() {
        cm.addPrimaryClipChangedListener(listener)
    }

    /**
     * Open search result or url.
     *
     * @param context
     * @param text
     */
    private fun searchOrBrowse(context: Context, text: CharSequence) {
        val query = text.toString()

        val url =
                if (Urls.isValidUrl(query)) query
                else UrlFactory()(context, PreferenceApplier(context).getDefaultSearchEngine(), query).toString()
        browserViewModel?.preview(url.toUri())
    }

    /**
     * Unregister listener.
     */
    fun dispose() {
        cm.removePrimaryClipChangedListener(listener)
    }

    companion object {

        /**
         * Disallow interval ms.
         */
        private const val DISALLOW_INTERVAL_MS: Long = 500L

        /**
         * Limit of text length.
         */
        private const val LENGTH_LIMIT = 40
    }
}
