package jp.toastkid.jitte.settings.background

import android.content.Context
import android.support.v7.app.AlertDialog
import android.text.Html

import jp.toastkid.jitte.R

/**
 * @author toastkidjp
 */
internal class ClearImages(private val context: Context, private val action: Runnable) {

    operator fun invoke() {
        AlertDialog.Builder(context)
                .setTitle(R.string.clear_all)
                .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { d, i ->
                    action.run()
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .show()
    }
}
