package jp.toastkid.yobidashi.libs.clip

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.network.NetworkChecker

/**
 * Clipping URL opener, this class invoked containing URL in clipboard.
 *
 * @author toastkidjp
 */
class ClippingUrlOpener {

    /**
     * Invoke action.
     *
     * @param view [View](Nullable)
     * @param onClick callback
     */
    operator fun invoke(context: Context?, onClick: (Uri) -> Unit) {
        if (context == null || NetworkChecker.isNotAvailable(context)) {
            return
        }

        val activityContext = context
        val clipboardContent = Clipboard.getPrimary(activityContext)?.toString() ?: return
        val preferenceApplier = PreferenceApplier(activityContext)
        val lastClipped = preferenceApplier.lastClippedWord()

        if (shouldNotFeedback(clipboardContent, lastClipped)) {
            return
        }

        preferenceApplier.setLastClippedWord(clipboardContent)

        feedbackToUser(context, clipboardContent, onClick)
    }

    private fun shouldNotFeedback(clipboardContent: String, lastClippedWord: String) =
            Urls.isInvalidUrl(clipboardContent)
                    || clipboardContent == lastClippedWord
                    || clipboardContent.length > 100

    private fun feedbackToUser(
            context: Context,
            clipboardContent: String,
            onClick: (Uri) -> Unit
    ) {
        val contentViewModel = (context as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
        }

        contentViewModel?.snackWithAction(
            context.getString(R.string.message_clipping_url_open, clipboardContent),
            context.getString(R.string.open),
            { onClick(clipboardContent.toUri()) }
        )
    }

}