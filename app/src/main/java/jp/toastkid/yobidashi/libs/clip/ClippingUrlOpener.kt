package jp.toastkid.yobidashi.libs.clip

import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
object ClippingUrlOpener {

    operator fun invoke(view: View?, onClick: (Uri) -> Unit) {
        if (view == null) {
            return
        }

        val activityContext = view.context
        val clipboardContent = Clipboard.getPrimary(activityContext)?.toString() ?: return

        if (Urls.isInvalidUrl(clipboardContent)) {
            return
        }

        Toaster.withAction(
                view,
                "Would you open \"$clipboardContent\"?",
                R.string.open,
                View.OnClickListener { onClick(clipboardContent.toUri()) },
                PreferenceApplier(activityContext).colorPair()
        )
    }
}