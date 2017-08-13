package jp.toastkid.yobidashi.search.clip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View

import io.reactivex.functions.Consumer
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * Search action with clipboard text.

 * @author toastkidjp
 */
class SearchWithClip
/**
 * Initialize with ClipboardManager, parent view, and color pair.

 * @param cm
 * *
 * @param parent
 * *
 * @param colorPair
 */
(
        /** For monitoring clipboard.  */
        private val cm: ClipboardManager,
        /** Use for showing snackbar.  */
        private val parent: View,
        /** Use for showing snackbar.  */
        private val colorPair: ColorPair,
        /** Use for browse clipped URL.  */
        private val browseCallback: Consumer<String>
) {

    /**
     * Invoke action.
     */
    operator fun invoke() {
        cm.addPrimaryClipChangedListener {
            if (!cm.hasPrimaryClip()) {
                return@cm.addPrimaryClipChangedListener
            }

            val firstItem = cm.primaryClip.getItemAt(0)
            if (firstItem == null) {
                return@cm.addPrimaryClipChangedListener
            }

            val text = firstItem!!.text
            if (text == null || text.length == 0) {
                return@cm.addPrimaryClipChangedListener
            }

            val context = parent.context
            Toaster.snackLong(
                    parent,
                    context.getString(R.string.message_clip_search, text),
                    R.string.title_search_action,
                    { v -> searchOrBrowse(context, text) },
                    colorPair
            )
        }
    }

    private fun searchOrBrowse(context: Context, text: CharSequence) {
        val query = text.toString()
        if (Urls.isValidUrl(query)) {
            try {
                browseCallback.accept(query)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return
        }
        SearchAction(context, SearchCategory.GOOGLE.name, query).invoke()
    }
}
