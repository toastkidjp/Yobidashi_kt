package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArraySet
import jp.toastkid.yobidashi.R

/**
 * Default colors.

 * @author toastkidjp
 */
internal object DefaultColors {

    fun make(context: Context): ArraySet<SavedColor> {
        val models = ArraySet<SavedColor>()
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.colorPrimaryDark), fromRes(context, R.color.textPrimary)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.colorPrimaryDark), fromRes(context, R.color.deep_yellow)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.crimson_bg), fromRes(context, R.color.crimson_font)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.cyan_bg), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.red_200_dd), fromRes(context, R.color.black)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.yellow_200_dd), fromRes(context, R.color.black)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.black), fromRes(context, R.color.textPrimary)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.indigo_200_dd), fromRes(context, R.color.black)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.white), fromRes(context, R.color.black)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.pinky), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.lime_bg), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.purple_bg), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.wa_bg), fromRes(context, R.color.wa_font)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.light_blue_200_dd), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.teal_500_dd), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.gray_500_dd), fromRes(context, R.color.white)))
        models.add(SavedColors.makeSavedColor(
                fromRes(context, R.color.deep_orange_500_dd), fromRes(context, R.color.white)))

        return models
    }

    @ColorInt private fun fromRes(
            context: Context, @ColorRes colorId: Int): Int {
        return ContextCompat.getColor(context, colorId)
    }
}
