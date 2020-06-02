package jp.toastkid.yobidashi.settings.color

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.collection.ArraySet
import androidx.core.content.ContextCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Default colors.
 *
 * @author toastkidjp
 */
class DefaultColorInsertion {

    private val map = mapOf(
            R.color.colorPrimaryDark to R.color.textPrimary,
            R.color.colorPrimaryDark to R.color.deep_yellow,
            R.color.crimson_bg to R.color.crimson_font,
            R.color.dark_brown to R.color.pink,
            R.color.cyan_bg to R.color.white,
            R.color.red_200_dd to R.color.black,
            R.color.yellow_200_dd to R.color.black,
            R.color.black to R.color.textPrimary,
            R.color.indigo_200_dd to R.color.black,
            R.color.white to R.color.black,
            R.color.pinky to R.color.white,
            R.color.lime_bg to R.color.white,
            R.color.purple_bg to R.color.white,
            R.color.wa_bg to R.color.wa_font,
            R.color.light_blue_200_dd to R.color.white,
            R.color.teal_500_dd to R.color.white,
            R.color.gray_500_dd to R.color.white,
            R.color.deep_orange_500_dd to R.color.white
    )

    /**
     * Insert default colors.
     *
     * @param context
     */
    @SuppressLint("CheckResult")
    fun insert(context: Context): Job =
            CoroutineScope(Dispatchers.IO).launch {
                val repository = DatabaseFinder().invoke(context).savedColorRepository()
                make(context).forEach { repository.add(it) }
            }

    private fun make(context: Context): ArraySet<SavedColor> {
        val models = ArraySet<SavedColor>()
        map.map {
            SavedColor.make(
                    fromRes(context, it.key),
                    fromRes(context, it.value)
            )
        }.forEach { models.add(it) }
        return models
    }

    @ColorInt
    private fun fromRes(context: Context, @ColorRes colorId: Int): Int =
            ContextCompat.getColor(context, colorId)

}
