package jp.toastkid.yobidashi.search.clip

import android.content.ClipboardManager
import android.content.Context
import android.view.View
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction

/**
 * Search action with clipboard text.
 * Initialize with ClipboardManager, parent view, and color pair.
 *
 * @param cm For monitoring clipboard.
 *
 * @param parent Use for showing snackbar.
 *
 * @param colorPair Use for showing snackbar.
 *
 * @param browseCallback Use for browse clipped URL.
 *
 * @author toastkidjp
 */
class SearchWithClip(
        private val cm: ClipboardManager,
        private val parent: View,
        private val colorPair: ColorPair,
        private val browseCallback: (String) -> Unit
) {

    private lateinit var listener: ClipboardManager.OnPrimaryClipChangedListener

    private var disposable: Disposable = Disposables.empty()

    /**
     * Invoke action.
     */
    operator fun invoke() {
        listener = ClipboardManager.OnPrimaryClipChangedListener{
            if (!PreferenceApplier(parent.context).enableSearchWithClip || !cm.hasPrimaryClip()) {
                return@OnPrimaryClipChangedListener
            }

            val firstItem = cm.primaryClip.getItemAt(0)
            firstItem ?: return@OnPrimaryClipChangedListener

            val text = firstItem.text
            if (text == null || text.isEmpty()) {
                return@OnPrimaryClipChangedListener
            }

            val context = parent.context
            Toaster.snackLong(
                    parent,
                    context.getString(R.string.message_clip_search, text),
                    R.string.title_search_action,
                    View.OnClickListener { v ->
                        disposable = searchOrBrowse(context, text) },
                    colorPair
            )
        }
        cm.addPrimaryClipChangedListener(listener)
    }

    private fun searchOrBrowse(context: Context, text: CharSequence): Disposable {
        val query = text.toString()
        if (Urls.isValidUrl(query)) {
            browseCallback(query)
            return Disposables.empty()
        }
        return SearchAction(context, PreferenceApplier(context).getDefaultSearchEngine(), query).invoke()
    }

    fun dispose() {
        cm.removePrimaryClipChangedListener(listener)
        disposable.dispose()
    }
}
