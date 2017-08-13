package jp.toastkid.yobidashi.libs.intent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.customtabs.CustomTabsIntent

import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * @author toastkidjp
 */
object CustomTabsFactory {

    fun make(
            context: Context,
            pair: ColorPair,
            @DrawableRes iconId: Int): CustomTabsIntent.Builder {
        return make(context, pair.bgColor(), pair.fontColor(), iconId)
    }

    /**

     * @param context
     * *
     * @param backgroundColor
     * *
     * @param fontColor
     * *
     * @param iconId
     * *
     * @return
     */
    fun make(
            context: Context,
            @ColorInt backgroundColor: Int,
            @ColorInt fontColor: Int,
            @DrawableRes iconId: Int): CustomTabsIntent.Builder {
        return CustomTabsIntent.Builder()
                .setToolbarColor(backgroundColor)
                .setShowTitle(true)
                .setCloseButtonIcon(decodeResource(context, iconId))
                .setSecondaryToolbarColor(fontColor)
                .setStartAnimations(context, 0, 0)
                .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .addDefaultShareMenuItem()
    }

    /**

     * @param context
     * *
     * @param id
     * *
     * @return
     */
    private fun decodeResource(
            context: Context,
            @DrawableRes id: Int
    ): Bitmap {
        return BitmapFactory.decodeResource(context.resources, id)
    }
}
