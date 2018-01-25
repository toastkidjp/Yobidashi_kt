package jp.toastkid.yobidashi.libs.db

import android.content.Context
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View

import com.github.gfx.android.orma.Deleter

import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Action of clear passed deleter.
 *
 * @param view View
 * @param deleter for deletion
 * @author toastkidjp
 */
class Clear(private val view: View, private val deleter: Deleter<*, *>) {

    /**
     * Context.
     */
    private val context: Context = view.context

    operator fun invoke(callback: () -> Unit = {}) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_delete_all)
                .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, i ->
                    deleter.executeAsSingle()
                            .subscribeOn(Schedulers.io())
                            .subscribe { v ->
                                Toaster.snackShort(
                                        view,
                                        R.string.settings_color_delete,
                                        PreferenceApplier(context).colorPair()
                                )
                                callback()
                                d.dismiss()
                            }
                }
                .show()
    }
}