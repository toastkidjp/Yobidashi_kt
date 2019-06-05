package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import android.view.View
import android.widget.TextView
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitializer
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
    ) = SavedColor().also {
        it.bgColor = bgColor
        it.fontColor = fontColor
    }

    /**
     * Delete all asynchronously.
     *
     * @param view
     * @param deleter
     * @param d
     */
    fun deleteAllAsync(
            view: View?,
            deleter: SavedColor_Deleter?
    ): Disposable {
        if (view == null || deleter == null) {
            return Disposables.empty()
        }
        return deleter.executeAsSingle()
                .subscribeOn(Schedulers.io())
                .subscribe { _, _ ->
                    Toaster.snackShort(
                            view,
                            R.string.settings_color_delete,
                            PreferenceApplier(view.context).colorPair()
                    )
                }
    }

    /**
     * Insert default colors.
     *
     * @param context
     */
    fun insertDefaultColors(context: Context) {
        Completable.fromAction {
            DbInitializer.init(context).relationOfSavedColor()
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
    fun insertRandomColors(context: Context): Disposable {

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

        return Completable.fromAction {
            DbInitializer.init(context).relationOfSavedColor()
                    .inserter()
                    .executeAsSingle(makeSavedColor(bg, font))
                    .subscribe()
        }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

}
