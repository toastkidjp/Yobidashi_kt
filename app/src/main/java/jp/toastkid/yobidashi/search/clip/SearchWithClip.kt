package jp.toastkid.yobidashi.search.clip

import android.content.ClipboardManager
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.network.NetworkChecker

/**
 * Search action with clipboard text.
 * Initialize with ClipboardManager, parent view, and color pair.
 *
 * @param clipboardManager For monitoring clipboard.
 * @param parent Use for showing snackbar.
 * @param colorPair Use for showing snackbar.
 * @param browserViewModel [BrowserViewModel].
 * @param preferenceApplier [PreferenceApplier]
 *
 * @author toastkidjp
 */
class SearchWithClip(
    private val clipboardManager: ClipboardManager,
    private val parent: View,
    private val colorPair: ColorPair,
    private val browserViewModel: BrowserViewModel?,
    private val preferenceApplier: PreferenceApplier
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
            val context = parent.context
            if (isInvalidCondition() || NetworkChecker.isNotAvailable(context)) {
                return@OnPrimaryClipChangedListener
            }

            val firstItem = clipboardManager.primaryClip?.getItemAt(0)
                    ?: return@OnPrimaryClipChangedListener

            val text = firstItem.text
            if (text.isNullOrEmpty()
                || (Urls.isInvalidUrl(text.toString()) && LENGTH_LIMIT <= text.length)
                || preferenceApplier.lastClippedWord() == text
            ) {
                return@OnPrimaryClipChangedListener
            }

            preferenceApplier.setLastClippedWord(text.toString())

            val contentViewModel = (context as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it).get(ContentViewModel::class.java)
            }

            contentViewModel?.snackWithAction(
                context.getString(R.string.message_clip_search, text),
                context.getString(R.string.title_search_action),
                { searchOrBrowse(text) }
            )
        }
    }

    /**
     * If it is invalid condition, return true.
     *
     * @return If it is invalid condition, return true.
     */
    private fun isInvalidCondition(): Boolean {
        return (!preferenceApplier.enableSearchWithClip
                || !clipboardManager.hasPrimaryClip()
                || (System.currentTimeMillis() - lastClipped) < DISALLOW_INTERVAL_MS)
    }

    /**
     * Invoke action.
     */
    operator fun invoke() {
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    /**
     * Open search result or url.
     *
     * @param text
     */
    private fun searchOrBrowse(text: CharSequence) {
        val query = text.toString()

        val url =
                if (Urls.isValidUrl(query)) query
                else UrlFactory()(preferenceApplier.getDefaultSearchEngine()
                        ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName(), query).toString()
        browserViewModel?.preview(url.toUri())
    }

    /**
     * Unregister listener.
     */
    fun dispose() {
        clipboardManager.removePrimaryClipChangedListener(listener)
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
