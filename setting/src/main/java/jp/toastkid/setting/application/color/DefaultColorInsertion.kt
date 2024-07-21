package jp.toastkid.setting.application.color

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.collection.ArraySet
import androidx.core.content.ContextCompat
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.yobidashi.settings.color.SavedColor
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
        jp.toastkid.lib.R.color.colorPrimaryDark to jp.toastkid.lib.R.color.deep_yellow,
        jp.toastkid.lib.R.color.crimson_bg to jp.toastkid.lib.R.color.crimson_font,
        jp.toastkid.lib.R.color.dark_brown to jp.toastkid.lib.R.color.pink,
        jp.toastkid.lib.R.color.cyan_bg to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.red_200_dd to jp.toastkid.lib.R.color.black,
        jp.toastkid.lib.R.color.yellow_200_dd to jp.toastkid.lib.R.color.black,
        jp.toastkid.lib.R.color.black to jp.toastkid.lib.R.color.deep_yellow,
        jp.toastkid.lib.R.color.indigo_200_dd to jp.toastkid.lib.R.color.black,
        jp.toastkid.lib.R.color.white to jp.toastkid.lib.R.color.black,
        jp.toastkid.lib.R.color.pinky to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.lime_bg to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.purple_bg to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.wa_bg to jp.toastkid.lib.R.color.wa_font,
        jp.toastkid.lib.R.color.light_blue_200_dd to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.teal_500_dd to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.gray_500_dd to jp.toastkid.lib.R.color.white,
        jp.toastkid.lib.R.color.deep_orange_500_dd to jp.toastkid.lib.R.color.white
    )

    /**
     * Insert default colors.
     *
     * @param context
     */
    @SuppressLint("CheckResult")
    fun insert(context: Context): Job =
            CoroutineScope(Dispatchers.IO).launch {
                val repository = RepositoryFactory().savedColorRepository(context)
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
