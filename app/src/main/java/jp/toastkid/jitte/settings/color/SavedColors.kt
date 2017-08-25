package jp.toastkid.jitte.settings.color

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
import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.db.DbInitter
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import java.util.*

/**
 * @author toastkidjp
 */
object SavedColors {

    internal fun setSaved(tv: TextView, color: SavedColor) {
        tv.setBackgroundColor(color.bgColor)
        tv.setTextColor(color.fontColor)
    }

    internal fun makeSavedColor(
            @ColorInt bgColor: Int,
            @ColorInt fontColor: Int
    ): SavedColor {
        val color = SavedColor()
        color.bgColor = bgColor
        color.fontColor = fontColor

        return color
    }

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

    private fun deleteAllAsync(
            view: View,
            deleter: SavedColor_Deleter,
            d: DialogInterface
    ) {
        deleter.executeAsSingle()
                .subscribeOn(Schedulers.io())
                .subscribe { t, t2 ->
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
     * @param context
     */
    fun insertDefaultColors(context: Context) {

        Completable.create { e ->
            DbInitter.get(context).relationOfSavedColor()
                    .inserter()
                    .executeAllAsObservable(DefaultColors.make(context))
                    .subscribe()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    fun insertRandomColor(context: Context) {

        val random = Random()
        @ColorInt val bg = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )
        @ColorInt val font = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )
        Completable.create { e ->
            DbInitter.get(context).relationOfSavedColor()
                    .inserter()
                    .executeAsSingle(makeSavedColor(bg, font))
                    .subscribe()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

}
