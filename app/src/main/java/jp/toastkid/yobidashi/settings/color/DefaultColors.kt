package jp.toastkid.yobidashi.settings.color

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.collection.ArraySet
import androidx.core.content.ContextCompat
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber

/**
 * Default colors.

 * @author toastkidjp
 */
internal object DefaultColors {

    fun make(context: Context): ArraySet<SavedColor> {
        val models = ArraySet<SavedColor>()
        models.add(SavedColor.make(
                fromRes(context, R.color.colorPrimaryDark), fromRes(context, R.color.textPrimary)))
        models.add(SavedColor.make(
                fromRes(context, R.color.colorPrimaryDark), fromRes(context, R.color.deep_yellow)))
        models.add(SavedColor.make(
                fromRes(context, R.color.crimson_bg), fromRes(context, R.color.crimson_font)))
        models.add(SavedColor.make(
                fromRes(context, R.color.dark_brown), fromRes(context, R.color.pink)))
        models.add(SavedColor.make(
                fromRes(context, R.color.cyan_bg), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.red_200_dd), fromRes(context, R.color.black)))
        models.add(SavedColor.make(
                fromRes(context, R.color.yellow_200_dd), fromRes(context, R.color.black)))
        models.add(SavedColor.make(
                fromRes(context, R.color.black), fromRes(context, R.color.textPrimary)))
        models.add(SavedColor.make(
                fromRes(context, R.color.indigo_200_dd), fromRes(context, R.color.black)))
        models.add(SavedColor.make(
                fromRes(context, R.color.white), fromRes(context, R.color.black)))
        models.add(SavedColor.make(
                fromRes(context, R.color.pinky), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.lime_bg), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.purple_bg), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.wa_bg), fromRes(context, R.color.wa_font)))
        models.add(SavedColor.make(
                fromRes(context, R.color.light_blue_200_dd), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.teal_500_dd), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.gray_500_dd), fromRes(context, R.color.white)))
        models.add(SavedColor.make(
                fromRes(context, R.color.deep_orange_500_dd), fromRes(context, R.color.white)))

        return models
    }

    @ColorInt private fun fromRes(
            context: Context, @ColorRes colorId: Int): Int {
        return ContextCompat.getColor(context, colorId)
    }

    /**
     * Insert default colors.
     *
     * @param context
     */
    @SuppressLint("CheckResult")
    fun insert(context: Context) {
        Completable.fromAction {
            val repository = DatabaseFinder().invoke(context).savedColorRepository()
            DefaultColors.make(context).forEach { repository.add(it) }
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {},
                        Timber::e
                )
    }
}
