package jp.toastkid.yobidashi.libs.clip

import android.net.Uri
import android.text.TextUtils
import android.view.View
import androidx.core.net.toUri
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Clipping URL opener, this class invoked containing URL in clipboard.
 *
 * @author toastkidjp
 */
object ClippingUrlOpener {

    private var previous: String? = null

    /**
     * Invoke action.
     *
     * @param view [View](Nullable)
     * @param onClick callback
     */
    operator fun invoke(view: View?, onClick: (Uri) -> Unit) {
        if (view == null) {
            return
        }

        val activityContext = view.context
        val clipboardContent = Clipboard.getPrimary(activityContext)?.toString() ?: return

        if (Urls.isInvalidUrl(clipboardContent) || TextUtils.equals(previous, clipboardContent)) {
            return
        }

        previous = clipboardContent

        Toaster.withAction(
                view,
                "Would you open \"$clipboardContent\"?",
                R.string.open,
                View.OnClickListener { onClick(clipboardContent.toUri()) },
                PreferenceApplier(activityContext).colorPair()
        )
    }
}