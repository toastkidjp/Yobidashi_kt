package jp.toastkid.jitte.libs.db

import android.content.Context
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View

import com.github.gfx.android.orma.Deleter

import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * Action of clear passed deleter.

 * @author toastkidjp
 */
class Clear(private val view: View, private val deleter: Deleter<*, *>) {

    private val context: Context

    init {
        this.context = view.context
    }

    operator fun invoke(callback: Runnable) {
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
                                callback.run()
                                d.dismiss()
                            }
                }
                .show()
    }
}