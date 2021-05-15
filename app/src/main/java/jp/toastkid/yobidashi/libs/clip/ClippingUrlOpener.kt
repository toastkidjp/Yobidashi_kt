package jp.toastkid.yobidashi.libs.clip

import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
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
    operator fun invoke(view: View?, onClick: (Uri) -> Unit) {
        if (view == null || NetworkChecker.isNotAvailable(view.context)) {
            return
        }

        val activityContext = view.context
        val clipboardContent = Clipboard.getPrimary(activityContext)?.toString() ?: return
        val preferenceApplier = PreferenceApplier(activityContext)
        val lastClipped = preferenceApplier.lastClippedWord()

        if (shouldNotFeedback(clipboardContent, lastClipped)) {
            return
        }

        preferenceApplier.setLastClippedWord(clipboardContent)

        feedbackToUser(view, clipboardContent, onClick)
    }

    private fun shouldNotFeedback(clipboardContent: String, lastClippedWord: String) =
            Urls.isInvalidUrl(clipboardContent) || clipboardContent == lastClippedWord

    private fun feedbackToUser(
            view: View,
            clipboardContent: String,
            onClick: (Uri) -> Unit
    ) {
        Toaster.withAction(
            view,
            view.context.getString(R.string.message_clipping_url_open, clipboardContent),
            R.string.open,
            { onClick(clipboardContent.toUri()) },
            PreferenceApplier(view.context).colorPair(),
            Snackbar.LENGTH_LONG
        )
    }

}