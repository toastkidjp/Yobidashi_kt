package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import android.widget.TextView
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber
import java.util.*

/**
 * @author toastkidjp
 */
object SavedColors {

    /**
     * Set saved colors.
     *
     * @param tv
     * @param color
     */
    internal fun setSaved(tv: TextView, color: SavedColor) {
        tv.setBackgroundColor(color.bgColor)
        tv.setTextColor(color.fontColor)
    }

    /**
     * Make saved color.
     *
     * @param bgColor
     * @param fontColor
     */
    internal fun makeSavedColor(
            @ColorInt bgColor: Int,
            @ColorInt fontColor: Int
    ): SavedColor {
        var color = SavedColor()
        color.bgColor = bgColor
        color.fontColor = fontColor

        return color
    }

    /**
     * Show clear colors dialog.
     *
     * @param context
     * @param view
     * @param relation
     */
    internal fun showClearColorsDialog(
            context: Context,
            view: View,
            relation: SavedColor_Relation
    ) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_clear_saved_color)
                .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, i -> deleteAllAsync(view, relation.deleter(), d) }
                .show()
    }

    /**
     * Delete all asynchronously.
     *
     * @param view
     * @param deleter
     * @param d
     */
    private fun deleteAllAsync(
            view: View,
            deleter: SavedColor_Deleter,
            d: DialogInterface
    ) {
        deleter.executeAsSingle()
                .subscribeOn(Schedulers.io())
                .subscribe { _, _ ->
                    Toaster.snackShort(
                            view,
                            R.string.settings_color_delete,
                            PreferenceApplier(view.context).colorPair()
                    )
                    d.dismiss()
                }
    }

    /**
     * Insert default colors.
     *
     * @param context
     */
    fun insertDefaultColors(context: Context) {
        Completable.fromAction {
            DbInitter.init(context).relationOfSavedColor()
                    .inserter()
                    .executeAllAsObservable(DefaultColors.make(context))
                    .subscribe()
        }.subscribeOn(Schedulers.io()).subscribe({}, {Timber.e(it)})
    }

    /**
     * Insert random colors.
     *
     * @param context
     */
    fun insertRandomColors(context: Context) {

        val random = Random()

        val bg = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )

        val font = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )

        Completable.fromAction {
            DbInitter.init(context).relationOfSavedColor()
                    .inserter()
                    .executeAsSingle(makeSavedColor(bg, font))
                    .subscribe()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

}
