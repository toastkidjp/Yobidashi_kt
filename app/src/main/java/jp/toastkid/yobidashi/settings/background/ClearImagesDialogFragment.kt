package jp.toastkid.yobidashi.settings.background

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * @author toastkidjp
 */
internal class ClearImagesDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickClearImages()
    }

    private var onClick: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)
        if (activityContext is Callback) {
            onClick = activityContext
        }

        val target = targetFragment
        if (target is Callback) {
            onClick = target
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.clear_all)
                .setMessage(HtmlCompat.fromHtml(activityContext.getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClick?.onClickClearImages()
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }
}
